/*
 * Copyright © 2024 moehreag <moehreag@gmail.com> & Contributors
 * Copyright © 2026 Dominic Seung <dominic@seung.dev>
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

package io.github.axolotlclient.modules.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.AxolotlClientConfig.api.options.OptionCategory;
import io.github.axolotlclient.util.GsonHelper;
import io.github.axolotlclient.util.Logger;
import lombok.Getter;

@Getter
public abstract class Accounts {

	public final OptionCategory category = OptionCategory.create("auth");

	private final List<Account> accounts = new ArrayList<>();
	protected Account current;
	protected MSApi msApi;

	public void load() {
		Path legacy = DolphinClientCommon.resolveConfigFile("../accounts.json");
		Path saveFile = getAccountsSaveFile();
		if (Files.exists(legacy)) {
			try {
				if (Files.exists(saveFile)) {
					Files.move(legacy, legacy.resolveSibling("accounts.json.old"));
					getLogger().info("Renaming legacy accounts save file since new one exists already.");
				} else {
					Files.createDirectories(saveFile.getParent());
					Files.move(legacy, saveFile);
				}
			} catch (IOException e) {
				getLogger().warn("Failed to move legacy accounts file to new location", e);
			}
		}
		if (Files.exists(saveFile)) {
			try {
				JsonObject list = GsonHelper.GSON.fromJson(Files.newBufferedReader(saveFile), JsonObject.class);
				if (list != null) {
					list.get("accounts").getAsJsonArray().forEach(jsonElement -> accounts.add(Account.deserialize(jsonElement.getAsJsonObject())));
				}
			} catch (IOException e) {
				getLogger().warn("Failed to load accounts file!", e);
			}
		} else {
			try {
				Files.createDirectories(getAccountsSaveFile().getParent());
				Files.createFile(getAccountsSaveFile());
			} catch (IOException e) {
				getLogger().warn("Failed to create accounts file", e);
			}
		}
	}

	protected Path getAccountsSaveFile() {
		return DolphinClientCommon.resolveConfigFile("accounts.json");
	}

	public void addAccount(Account account) {
		accounts.add(account);
	}

	protected abstract void login(Account account);

	public void removeAccount(Account account) {
		accounts.remove(account);
		save();
	}

	public void save() {
		JsonArray array = new JsonArray();
		accounts.forEach(account -> array.add(account.serialize()));
		JsonObject object = new JsonObject();
		object.add("accounts", array);
		try {
			Files.createDirectories(getAccountsSaveFile().getParent());
			Files.writeString(getAccountsSaveFile(), GsonHelper.GSON.toJson(object));
		} catch (IOException e) {
			getLogger().error("Failed to save acounts config!", e);
		}
	}

	private Logger getLogger() {
		return DolphinClientCommon.getInstance().getLogger();
	}

	protected boolean isContained(String uuid) {
		return accounts.stream().anyMatch(account -> account.getUuid().equals(uuid));
	}

	public boolean allowOfflineAccounts() {
		return !accounts.isEmpty() && !accounts.stream().allMatch(Account::isOffline);
	}

	abstract CompletableFuture<Account> showAccountsExpiredScreen(Account account);

	abstract void displayDeviceCode(DeviceFlowData data);
}
