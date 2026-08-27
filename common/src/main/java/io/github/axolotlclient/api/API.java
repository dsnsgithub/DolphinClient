/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 DSNS <dominic@seung.dev>
 *
 * This file is part of DolphinClient, a fork of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.api;

import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.api.requests.GlobalDataRequest;
import io.github.axolotlclient.api.util.Authentication;
import io.github.axolotlclient.api.util.MojangAuth;
import io.github.axolotlclient.modules.auth.Account;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.NetworkUtil;
import io.github.axolotlclient.util.ThreadExecuter;
import io.github.axolotlclient.util.notifications.NotificationProvider;
import lombok.Getter;

/**
 * Client for the backend the Hypixel modules and image sharing talk to.
 */
public class API {

	@Getter
	private static API Instance;
	@Getter
	private final Logger logger;
	@Getter
	private final NotificationProvider notificationProvider = DolphinClientCommon.getInstance().getNotificationProvider();

	@Getter
	private final Options apiOptions;
	/**
	 * The undashed UUID of the account this session is authenticated as, or {@code null} while unauthenticated.
	 */
	@Getter
	private String selfUuid;
	private Account account;
	private Authentication auth;
	private HttpClient client;
	private CompletableFuture<?> restartingFuture;
	private int nextRestartSecs;
	private final AtomicBoolean starting = new AtomicBoolean();

	public API() {
		if (Instance != null) {
			throw new IllegalStateException("API may only be instantiated once!");
		}
		this.logger = DolphinClientCommon.getInstance().getLogger();
		this.apiOptions = DolphinClientCommon.getInstance().getApiOptions();
		Instance = this;
	}

	private CompletableFuture<?> authenticate() {
		//if (client != null) {
		// We have to rely on the gc to collect previous client objects as close() was only implemented in java 21.
		// However, we are currently compiling against java 17.
		//client.close();
		//}

		try {
			if (!GlobalDataRequest.get(true).get(1, TimeUnit.MINUTES).success()) {
				logger.warn("Not trying to start API as it couldn't be reached!");
				return scheduleRestart();
			}
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			logger.warn("Not trying to start API as it couldn't be reached within the timeout of 1 minute!");
			return scheduleRestart();
		}

		nextRestartSecs = 2;
		logDetailed("Authenticating with Mojang...");

		MojangAuth.Result result = MojangAuth.authenticate(account);

		if (result.getStatus() != MojangAuth.Status.SUCCESS) {
			logger.error("Failed to authenticate with Mojang! Status: ", result.getStatus());
			return CompletableFuture.failedFuture(new UnsupportedOperationException("Failed to authenticate with mojang, status: " + result.getStatus()));
		}

		logDetailed("Requesting authentication from backend...");

		return get(Request.Route.AUTHENTICATE.builder()
			.query("username", account.getName())
			.query("server_id", result.getServerId())
			.build()).whenComplete((response, throwable) -> {

			if (throwable != null) {
				logger.error("Failed to authenticate!", throwable);
				return;
			}
			if (response.isError()) {
				logger.error("Failed to authenticate!", response.getError().description());
				return;
			}

			auth = new Authentication(response.getBody("access_token"));
			selfUuid = sanitizeUUID(account.getUuid());
			logDetailed("Obtained token!");
		});
	}

	public CompletableFuture<Response> get(Request request) {
		return request(request, "GET");
	}

	public CompletableFuture<Response> patch(Request request) {
		return request(request, "PATCH");
	}

	public CompletableFuture<Response> post(Request request) {
		return request(request, "POST");
	}

	public CompletableFuture<Response> delete(Request request) {
		return request(request, "DELETE");
	}

	private CompletableFuture<Response> request(Request request, String method) {
		if (!getApiOptions().privacyAccepted.get().isAccepted()) {
			return CompletableFuture.completedFuture(Response.CLIENT_ERROR);
		}
		if (request.requiresAuthentication() && !isAuthenticated()) {
			logger.debug("Tried to request {} {} without authentication, but this request requires it!", method, request);
			return CompletableFuture.completedFuture(Response.CLIENT_ERROR);
		}
		URI route = getUrl(request);
		return request(route, request.bodyFields(), request.rawBody(), method, request.headers());
	}

	private CompletableFuture<Response> request(URI url, Map<String, ?> payload, byte[] rawBody, String method, Map<String, String> headers) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				logDetailed("Starting request to " + method + " " + url);

				HttpRequest.Builder builder = HttpRequest.newBuilder(url)
					.header("Content-Type", "application/json")
					.header("Accept", "application/json");

				if (auth != null) {
					if (auth.expiration().isBefore(Instant.now())) {
						authenticate().join();
					}
					builder.header("Authorization", auth.token());
				}

				if (headers != null) {
					headers.forEach(builder::header);
				}

				if (rawBody != null) {
					builder.method(method, HttpRequest.BodyPublishers.ofByteArray(rawBody));
				} else if (!(payload == null || payload.isEmpty())) {
					StringBuilder body = new StringBuilder();
					GsonHelper.GSON.toJson(payload, body);
					logDetailed("Sending payload: \n" + body);
					builder.method(method, HttpRequest.BodyPublishers.ofString(body.toString()));
				} else {
					builder.method(method, HttpRequest.BodyPublishers.noBody());
				}
				if (client == null) {
					client = NetworkUtil.createHttpClient();
				}

				HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());

				String body = response.body();

				int code = response.statusCode();
				if (!url.getPath().endsWith(Request.Route.AUTHENTICATE.getPath())) {
					logDetailed("Response: code: " + code + " body: " + body);
				} else {
					logDetailed("Response: code: " + code + " body: " + String.valueOf(body).replaceAll("(\"access_token\": ?\")[^\"]+(\")", "$1[token redacted]$2"));
				}
				return Response.builder().body(body).status(code).headers(response.headers().map()).build();
			} catch (ConnectException | HttpTimeoutException e) {
				logger.warn("Backend unreachable!");
				return Response.CLIENT_ERROR;
			} catch (Exception e) {
				onError(e);
				return Response.CLIENT_ERROR;
			}
		}, ThreadExecuter.service());
	}

	URI getUrl(Request request) {
		StringBuilder url = new StringBuilder(Constants.API_URL.endsWith("/") ? Constants.API_URL : Constants.API_URL + "/");
		url.append(request.route().getPath());
		if (request.path() != null) {
			for (String p : request.path()) {
				url.append("/").append(p);
			}
		}
		if (request.query() != null && !request.query().isEmpty()) {
			url.append("?");
			request.query().forEach((v) -> {
				if (url.charAt(url.length() - 1) != '?') {
					url.append("&");
				}
				url.append(v);
			});
		}
		return URI.create(url.toString());
	}

	public void shutdown() {
		if (restartingFuture != null) {
			restartingFuture.cancel(true);
			restartingFuture = null;
		}
		if (isAuthenticated()) {
			logger.debug("Shutting down API");
			// We have to rely on the gc to collect previous client objects as close() was only implemented in java 21.
			// However, we are currently compiling against java 17.
			//client.close();
			auth = null;
			selfUuid = null;
		}
		client = null;
	}

	public boolean isAuthenticated() {
		return auth != null;
	}

	public int getIndicatorColor() {
		return isAuthenticated() ? 0xFF009000 : 0xFFFF0000;
	}

	public void logDetailed(String message, Object... args) {
		logger.debug("[DETAIL] " + message, args);
	}

	public void onError(Throwable throwable) {
		logger.error("Error while handling API traffic:", throwable);
	}

	private CompletableFuture<?> scheduleRestart() {
		if (restartingFuture != null) {
			restartingFuture.cancel(true);
		}
		nextRestartSecs = Math.max(2, Math.min(nextRestartSecs * 2, 60*3));

		logger.info("Trying restart in " + nextRestartSecs + " seconds.");
		restartingFuture = CompletableFuture.supplyAsync(() -> {
			logDetailed("Restarting API session...");
			return startup(account).join();
		}, CompletableFuture.delayedExecutor(nextRestartSecs, TimeUnit.SECONDS, ThreadExecuter.service()));
		return restartingFuture;
	}

	public void restart() {
		shutdown();
		if (account != null) {
			startup(account);
		}
	}

	public CompletableFuture<?> startup(Account account) {
		this.account = account;
		if (!Constants.ENABLED) {
			return CompletableFuture.failedFuture(new UnsupportedOperationException("API is disabled at compile-time"));
		}

		if (account.isOffline()) {
			return CompletableFuture.failedFuture(new UnsupportedOperationException("Account is offline"));
		}

		switch (apiOptions.privacyAccepted.get()) {
			case UNSET -> {
				return apiOptions.openPrivacyNoteScreen.get().thenCompose(v ->
					v ? startupAPI() : CompletableFuture.failedStage(new UnsupportedOperationException("Terms not accepted")));
			}
			case ACCEPTED -> {
				return startupAPI();
			}
		}
		return CompletableFuture.failedFuture(new UnsupportedOperationException("API is disabled"));
	}

	private CompletableFuture<?> startupAPI() {
		if (isAuthenticated()) {
			logger.warn("API is already running!");
			return CompletableFuture.failedFuture(new UnsupportedOperationException("API is already running"));
		}
		if (Constants.TESTING) {
			return CompletableFuture.failedFuture(new UnsupportedOperationException("API is disabled for testing!"));
		}
		if (starting.getAndSet(true)) {
			return CompletableFuture.completedFuture(null);
		}

		logger.info("Starting API...");
		return CompletableFuture.runAsync(() -> {
			this.authenticate().join();
			starting.set(false);
		}, ThreadExecuter.service());
	}

	public static String sanitizeUUID(String uuid) {
		if (uuid.contains("-")) {
			return validateUUID(uuid.replace("-", ""));
		}
		return validateUUID(uuid);
	}

	private static String validateUUID(String uuid) {
		if (uuid.length() != 32) {
			throw new IllegalArgumentException("Not a valid UUID (undashed): " + uuid);
		}
		return uuid;
	}
}
