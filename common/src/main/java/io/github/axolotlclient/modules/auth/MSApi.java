/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
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

package io.github.axolotlclient.modules.auth;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import com.github.mizosoft.methanol.FormBodyPublisher;
import com.github.mizosoft.methanol.MediaType;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import com.google.common.hash.Hashing;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.modules.auth.skin.Cape;
import io.github.axolotlclient.modules.auth.skin.Skin;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.JsonBuilders;
import io.github.axolotlclient.util.Logger;
import io.github.axolotlclient.util.NetworkUtil;
import lombok.ToString;

// Partly oriented on In-Game-Account-Switcher by The-Fireplace, VidTu
public class MSApi {

	private static final String CLIENT_ID = "938592fc-8e01-4c6d-b56d-428c7d9cf5ea"; // AxolotlClient MSA ClientID
	private static final String SCOPES = "XboxLive.signin offline_access";
	private static final String XBL_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate";
	private static final String MS_DEVICE_CODE_LOGIN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode?mkt=";
	private static final String MS_TOKEN_LOGIN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
	private static final String XBL_XSTS_AUTH_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
	private static final String MC_LOGIN_WITH_XBOX_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";

	private final Supplier<String> languageSupplier;
	private final Logger logger;
	private final Accounts accounts;
	private final HttpClient client;

	public static MSApi INSTANCE;

	public MSApi(Accounts accounts, Supplier<String> languageSupplier) {
		this.logger = DolphinClientCommon.getInstance().getLogger();
		this.client = NetworkUtil.createHttpClient();
		this.accounts = accounts;
		this.languageSupplier = languageSupplier;
		INSTANCE = this;
	}

	public CompletableFuture<Account> startDeviceAuth() {

		String[] lang = languageSupplier.get().replace("_", "-").split("-");
		logger.debug("starting ms device auth flow");
		// https://learn.microsoft.com/en-us/entra/identity-platform/v2-oauth2-device-code#device-authorization-response
		HttpRequest.Builder builder = HttpRequest.newBuilder()
			.POST(FormBodyPublisher.newBuilder()
				.query("client_id", CLIENT_ID)
				.query("scope", SCOPES).build())
			.header("content-type", "application/x-www-form-urlencoded")
			.uri(URI.create(MS_DEVICE_CODE_LOGIN_URL + lang[0] + "-" + lang[1].toUpperCase(Locale.ROOT)));
		return requestJson(builder.build())
			.thenApplyAsync(object -> {
				int expiresIn = object.get("expires_in").getAsInt();
				String deviceCode = object.get("device_code").getAsString();
				String userCode = object.get("user_code").getAsString();
				String verificationUri = object.get("verification_uri").getAsString();
				int interval = object.get("interval").getAsInt();
				String message = object.get("message").getAsString();
				logger.debug("displaying device code to user");
				DeviceFlowData data = new DeviceFlowData(message, verificationUri, deviceCode, userCode, expiresIn, interval);
				accounts.displayDeviceCode(data);
				return data;
			}).thenComposeAsync(data -> {
				logger.debug("waiting for user authorization...");
				long start = System.currentTimeMillis();
				while (System.currentTimeMillis() - start < data.getExpiresIn() * 1000L && !data.isCancelled()) {
					if ((System.currentTimeMillis() - start) % data.getInterval() == 0) {
						HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().POST(
								FormBodyPublisher.newBuilder().query("client_id", CLIENT_ID)
									.query("device_code", data.getDeviceCode())
									.query("grant_type", "urn:ietf:params:oauth:grant-type:device_code")
									.build()
							)
							.uri(URI.create(MS_TOKEN_LOGIN_URL));
						JsonObject response = requestJson(requestBuilder.build()).join();

						if (response.has("refresh_token") && response.has("access_token")) {
							data.setStatus("auth.working");
							return authenticateFromMSTokens(response.get("access_token").getAsString(),
								response.get("refresh_token").getAsString())
								.thenApply(a -> {
									int index = accounts.getAccounts().indexOf(a);
									Account loginAccount;
									if (index == -1) {
										accounts.getAccounts().add(a);
										loginAccount = a;
									} else {
										var prev = accounts.getAccounts().get(index);
										prev.setAuthToken(a.getAuthToken());
										prev.setExpiration(a.getExpiration());
										prev.setMsaToken(a.getMsaToken());
										prev.setName(a.getName());
										prev.setRefreshToken(a.getRefreshToken());
										loginAccount = prev;
									}
									accounts.login(loginAccount);
									accounts.save();
									data.setStatus("auth.finished");
									return loginAccount;
								});
						}

						if (response.has("error")) {
							String error = response.get("error").getAsString();
							switch (error) {
								case "authorization_pending":
									continue;
								case "bad_verification_code":
									throw new IllegalStateException("Bad verification code! " + response);
								case "authorization_declined":
								case "expired_token":
								default:
									break;
							}
						}
					}
				}
				return CompletableFuture.failedStage(new TimeoutException());
			});
	}

	private CompletableFuture<Account> authenticateFromMSTokens(String accessToken, String refreshToken) {
		return CompletableFuture.supplyAsync(() -> {
			logger.debug("getting xbl token... ");
			XblData xbl = authXbl(accessToken).join();
			logger.debug("getting xsts token...");
			XblData xsts = authXstsMC(xbl.token()).join();
			logger.debug("getting mc auth token...");
			MCXblData mc = authMC(xsts.displayClaims().uhs(), xsts.token()).join();

			JsonObject profileJson = getMCProfile(mc.accessToken()).join();
			if (profileJson.has("error") && "NOT_FOUND".equals(profileJson.get("error").getAsString())) {
				DolphinClientCommon.getInstance().getNotificationProvider().addStatus("auth.notif.login.failed", "auth.notif.login.failed.no_profile");
				throw new IllegalStateException();
			}
			logger.debug("retrieving entitlements...");
			if (!checkOwnership(mc.accessToken()).join()) {
				DolphinClientCommon.getInstance().getNotificationProvider().addStatus("auth.notif.login.failed", "auth.notif.login.failed.no_entitlement");
				logger.warn("Failed to check for game ownership!");
				throw new IllegalStateException();
			}
			logger.debug("getting profile...");
			MCProfile profile = MCProfile.get(profileJson);
			return new Account(profile.name(), profile.id(), mc.accessToken(), mc.expiration(), refreshToken, accessToken);
		});
	}

	public record MCProfile(String id, String name, List<OnlineSkin> skins, List<OnlineCape> capes) {
		public static MCProfile get(JsonObject json) {
			var skinList = GsonHelper.jsonArrayToStream(json.getAsJsonArray("skins"))
				.map(s -> OnlineSkin.get(s.getAsJsonObject()))
				.toList();
			var capesList = GsonHelper.jsonArrayToStream(json.getAsJsonArray("capes"))
				.map(s -> OnlineCape.get(s.getAsJsonObject()))
				.toList();
			CompletableFuture.allOf(skinList.toArray(CompletableFuture[]::new));
			CompletableFuture.allOf(capesList.toArray(CompletableFuture[]::new));
			return new MCProfile(json.get("id").getAsString(), json.get("name").getAsString(),
				skinList.stream().map(CompletableFuture::join).toList(),
				capesList.stream().map(CompletableFuture::join).toList());
		}

		@ToString
		public static final class OnlineSkin implements Skin.Online {
			public static final String VARIANT_CLASSIC = "CLASSIC";
			public static final String VARIANT_SLIM = "SLIM";
			public static final String STATE_ACTIVE = "ACTIVE";
			private final String id;
			private final String state;
			private final String url;
			private boolean classicVariant;
			private final byte[] image;
			private final String textureKey;

			public OnlineSkin(String id, String state, String url, String variant,
							  byte[] image, String textureKey) {
				this.id = id;
				this.state = state;
				this.url = url;
				this.classicVariant = VARIANT_CLASSIC.equals(variant);
				this.image = image;
				this.textureKey = textureKey;
			}

			@SuppressWarnings("UnstableApiUsage")
			public static CompletableFuture<OnlineSkin> get(JsonObject object) {
				String url = object.get("url").getAsString();
				return INSTANCE.client.sendAsync(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofByteArray())
					.thenApplyAsync(res -> {
						if (res.statusCode() == 200) {
							return res.body();
						}
						throw new IllegalArgumentException("abnormal status: " + res.statusCode());
					}).thenApply(bytes -> new OnlineSkin(object.get("id").getAsString(),
						object.get("state").getAsString(),
						url,
						object.get("variant").getAsString(), bytes,
						Hashing.sha256().hashBytes(bytes).toString()));
			}

			public byte[] image() {
				return image;
			}

			public boolean classicVariant() {
				return classicVariant;
			}

			@Override
			public void classicVariant(boolean classic) {
				this.classicVariant = classic;
			}

			public boolean active() {
				return STATE_ACTIVE.equals(state());
			}

			@Override
			public CompletableFuture<MCProfile> equip(MSApi api, Account account) {
				return api.setSkin(account, this);
			}

			@Override
			public boolean supportsDownload() {
				return true;
			}

			public String id() {
				return id;
			}

			public String state() {
				return state;
			}

			@Override
			public String url() {
				return url;
			}

			@Override
			public String sha256() {
				return textureKey;
			}
		}

		public record OnlineCape(String id, String state, String url, String alias, byte[] image,
								 String sha256) implements Cape {
			public static final String STATE_ACTIVE = "ACTIVE";

			@SuppressWarnings("UnstableApiUsage")
			public static CompletableFuture<OnlineCape> get(JsonObject object) {
				String url = object.get("url").getAsString();
				return INSTANCE.client.sendAsync(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofByteArray())
					.thenApplyAsync(res -> {
						if (res.statusCode() == 200) {
							return res.body();
						}
						throw new IllegalArgumentException("abnormal status: " + res.statusCode());
					}).thenApply(bytes -> new OnlineCape(object.get("id").getAsString(), object.get("state").getAsString(),
						url, object.get("alias").getAsString(), bytes, Hashing.sha256().hashBytes(bytes).toString()));
			}

			public boolean active() {
				return STATE_ACTIVE.equals(state());
			}

			@Override
			public CompletableFuture<MCProfile> equip(MSApi api, Account account) {
				return api.showCape(account, this);
			}
		}
	}

	private CompletableFuture<XblData> authXbl(String code) {
		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
			.uri(URI.create(XBL_AUTH_URL))
			.POST(HttpRequest.BodyPublishers.ofString(JsonBuilders.JsonObject.create()
				.field("Properties", JsonBuilders.JsonObject.create()
					.field("AuthMethod", "RPS")
					.field("SiteName", "user.auth.xboxlive.com")
					.field("RpsTicket", "d=" + code))
				.field("RelyingParty", "http://auth.xboxlive.com")
				.field("TokenType", "JWT").asString()))
			.header("content-type", "application/json")
			.header("accept", "application/json");

		return requestJson(requestBuilder.build()).thenApply(response -> new XblData(Instant.parse(response.get("IssueInstant").getAsString()), Instant.parse(response.get("NotAfter").getAsString()),
			response.get("Token").getAsString(), new XblData.DisplayClaims(response.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString())));
	}

	private record XblData(Instant issueInstant, Instant notAfter, String token, DisplayClaims displayClaims) {
		private record DisplayClaims(String uhs) {
		}
	}

	private CompletableFuture<XblData> authXstsMC(String xblToken) {
		var body = JsonBuilders.JsonObject.create()
			.field("Properties", JsonBuilders.JsonObject.create()
				.field("SandboxId", "RETAIL")
				.field("UserTokens", JsonBuilders.JsonArray.create().field(xblToken)))
			.field("RelyingParty", "rp://api.minecraftservices.com/")
			.field("TokenType", "JWT");
		return requestJson(HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(body.asString())).uri(URI.create(XBL_XSTS_AUTH_URL)).build())
			.thenApply(response -> new XblData(Instant.parse(response.get("IssueInstant").getAsString()), Instant.parse(response.get("NotAfter").getAsString()),
				response.get("Token").getAsString(), new XblData.DisplayClaims(response.get("DisplayClaims").getAsJsonObject().get("xui").getAsJsonArray().get(0).getAsJsonObject().get("uhs").getAsString())));
	}

	private CompletableFuture<MCXblData> authMC(String userhash, String xsts) {
		var body = JsonBuilders.JsonObject.create().field("identityToken", "XBL3.0 x=" + userhash + ";" + xsts);
		return requestJson(HttpRequest.newBuilder(URI.create(MC_LOGIN_WITH_XBOX_URL)).POST(HttpRequest.BodyPublishers.ofString(body.asString())).build())
			.thenApply(response -> new MCXblData(response.get("username").getAsString(),
				response.get("access_token").getAsString(),
				Instant.now().plus(response.get("expires_in").getAsLong(), ChronoUnit.SECONDS)));
	}

	private record MCXblData(String username, String accessToken, Instant expiration) {
	}

	private CompletableFuture<Boolean> checkOwnership(String accessToken) {
		return requestJson(HttpRequest
			.newBuilder(URI.create("https://api.minecraftservices.com/entitlements/mcstore"))
			.header("Authorization", "Bearer " + accessToken).build())
			.thenApply(res -> GsonHelper.jsonArrayToStream(res.get("items").getAsJsonArray())
				.anyMatch(e -> e.isJsonObject() && e.getAsJsonObject().has("name")
					&& "game_minecraft".equals(e.getAsJsonObject().get("name").getAsString())));
	}

	private CompletableFuture<JsonObject> getMCProfile(String accessToken) {
		return requestJson(HttpRequest.newBuilder().GET()
			.uri(URI.create("https://api.minecraftservices.com/minecraft/profile"))
			.header("Authorization", "Bearer " + accessToken).build());
	}

	public CompletableFuture<Account> refresh(Account account) {
		logger.debug("refreshing auth code...");
		return requestJson(HttpRequest
			.newBuilder(URI.create(MS_TOKEN_LOGIN_URL))
			.POST(FormBodyPublisher.newBuilder()
				.query("client_id", CLIENT_ID)
				.query("refresh_token", account.getRefreshToken())
				.query("scope", SCOPES)
				.query("grant_type", "refresh_token").build())
			.header("Accept", "application/json").build())
			.thenCompose(response -> {
				if (response.has("error_codes")) {
					int errorCode = response.get("error_codes").getAsJsonArray().get(0).getAsInt();
					if (errorCode == 70000 || errorCode == 70012) {
						return accounts.showAccountsExpiredScreen(account);
					} else {
						logger.warn("Login error, unexpected response: " + response);
						DolphinClientCommon.getInstance().getNotificationProvider().addStatus("auth.notif.refresh.error", "auth.notif.refresh.error.unexpected_response");
						throw new IllegalArgumentException();
					}
				}
				logger.debug("authenticating...");
				return authenticateFromMSTokens(response.get("access_token").getAsString(),
					response.get("refresh_token").getAsString()).thenApply(refreshed -> {
					account.setRefreshToken(refreshed.getRefreshToken());
					account.setAuthToken(refreshed.getAuthToken());
					account.setName(refreshed.getName());
					account.setMsaToken(refreshed.getMsaToken());
					account.setExpiration(refreshed.getExpiration());
					accounts.save();
					return account;
				});
			});
	}

	private CompletableFuture<JsonObject> requestJson(HttpRequest request) {
		return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
			.thenApply(res -> GsonHelper.fromJson(res.body()));
	}

	public CompletableFuture<MCProfile> getProfile(Account account) {
		return getMCProfile(account.getAuthToken()).thenApply(this::extractProfile);
	}

	public record SkinBundle(String name, String id, CompletableFuture<byte[]> skin, String skinKey,
							 boolean classicModel) {
	}

	public CompletableFuture<SkinBundle> getTextures(String uuid) {
		return requestJson(HttpRequest.newBuilder().GET()
			.uri(URI.create("https://sessionserver.mojang.com/session/minecraft/profile/" + uuid))
			.build()).thenApply(json -> {
			var name = json.get("name").getAsString();
			var id = json.get("id").getAsString();
			var properties = json.get("properties").getAsJsonArray();
			for (JsonElement e : properties) {
				if (e.isJsonObject()) {
					var obj = e.getAsJsonObject();
					if (obj.has("name") && "textures".equals(obj.get("name").getAsString())) {
						var b64 = obj.get("value").getAsString();
						var props = GsonHelper.fromJson(new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8));
						var textures = props.get("textures").getAsJsonObject();
						if (textures.has("SKIN")) {
							var skinObj = textures.get("SKIN").getAsJsonObject();
							var skinUrl = skinObj.get("url").getAsString();
							var skin = client.sendAsync(HttpRequest.newBuilder().uri(URI.create(skinUrl)).GET().build(), HttpResponse.BodyHandlers.ofByteArray())
								.thenApply(HttpResponse::body);
							var skinKey = skinUrl.substring(skinUrl.lastIndexOf("/") + 1);
							var classicModel = true;
							if (skinObj.has("metadata")) {
								var metadata = skinObj.get("metadata").getAsJsonObject();
								var model = metadata.get("model").getAsString();
								classicModel = MCProfile.OnlineSkin.VARIANT_CLASSIC.toLowerCase(Locale.ROOT).equals(model.toLowerCase(Locale.ROOT));
							}
							return new SkinBundle(name, id, skin, skinKey, classicModel);
						}
					}
				}
			}
			return null;
		});
	}

	private MCProfile extractProfile(JsonObject profileJson) {
		if (profileJson.has("error") && "NOT_FOUND".equals(profileJson.get("error").getAsString())) {
			throw new IllegalStateException("profile not found");
		}
		if (!profileJson.has("id")) {
			logger.warn("Unexpected profile response: {}", profileJson);
			throw new IllegalStateException("unexpected error");
		}
		return MCProfile.get(profileJson);
	}


	public CompletableFuture<MCProfile> setSkin(Account account, Skin.Online skin) {
		return setSkin(account, skin.classicVariant(), skin.url());
	}

	public CompletableFuture<MCProfile> setSkin(Account account, boolean wide, String url) {
		return requestJson(HttpRequest.newBuilder()
			.uri(URI.create("https://api.minecraftservices.com/minecraft/profile/skins"))
			.header("Authorization", "Bearer " + account.getAuthToken())
			.POST(HttpRequest.BodyPublishers.ofString(JsonBuilders.JsonObject.create()
				.field("variant", wide ? MCProfile.OnlineSkin.VARIANT_CLASSIC : MCProfile.OnlineSkin.VARIANT_SLIM)
				.field("url", url).asString())).build())
			.thenApply(this::extractProfile);
	}

	public CompletableFuture<MCProfile> uploadAndSetSkin(Account account, boolean wide, byte[] image) {
		return uploadAndSetSkin(account, MultipartBodyPublisher.newBuilder()
			.textPart("variant", wide ? "classic" : "slim")
			.formPart("file", HttpRequest.BodyPublishers.ofByteArray(image)).build());
	}

	public CompletableFuture<MCProfile> uploadAndSetSkin(Account account, Skin.LocalSkin skin) {
		try {
			return uploadAndSetSkin(account, MultipartBodyPublisher.newBuilder()
				.textPart("variant", skin.classicVariant() ? "classic" : "slim")
				.filePart("file", skin.file(), MediaType.IMAGE_PNG).build());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private CompletableFuture<MCProfile> uploadAndSetSkin(Account account, MultipartBodyPublisher publisher) {
		return requestJson(HttpRequest.newBuilder()
			.uri(URI.create("https://api.minecraftservices.com/minecraft/profile/skins"))
			.header("Authorization", "Bearer " + account.getAuthToken())
			.POST(publisher).build())
			.thenApply(this::extractProfile);
	}

	public CompletableFuture<MCProfile> resetSkin(Account account) {
		return requestJson(HttpRequest.newBuilder().DELETE()
			.uri(URI.create("https://api.minecraftservices.com/minecraft/profile/skins/active"))
			.header("Authorization", "Bearer " + account.getAuthToken())
			.build())
			.thenApply(this::extractProfile);
	}

	public CompletableFuture<MCProfile> hideCape(Account account) {
		return requestJson(HttpRequest.newBuilder().DELETE()
			.uri(URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active"))
			.header("Authorization", "Bearer " + account.getAuthToken())
			.build())
			.thenApply(this::extractProfile);
	}

	public CompletableFuture<MCProfile> showCape(Account account, MCProfile.OnlineCape cape) {
		return requestJson(HttpRequest.newBuilder()
			.uri(URI.create("https://api.minecraftservices.com/minecraft/profile/capes/active"))
			.header("Authorization", "Bearer " + account.getAuthToken())
			.PUT(HttpRequest.BodyPublishers.ofString(JsonBuilders.JsonObject.create().field("capeId", cape.id()).asString())).build())
			.thenApply(this::extractProfile);
	}
}
