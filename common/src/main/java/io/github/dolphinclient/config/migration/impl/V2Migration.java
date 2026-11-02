/*
 * Copyright © 2025 moehreag <moehreag@gmail.com> & Contributors
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

package io.github.dolphinclient.config.migration.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.axolotlclient.AxolotlClientConfig.impl.options.GraphicsOption;
import io.github.dolphinclient.config.migration.ConfigMigration;
import io.github.dolphinclient.util.GsonHelper;

public class V2Migration implements ConfigMigration {
	@Override
	public int version() {
		return 2;
	}

	@Override
	public void apply(JsonObject json) {
		getObject(json, "hud").ifPresent(hud -> {
			getObject(hud, "keystrokehud").ifPresent(keystrokes -> {
				var mousemovement = new JsonObject();
				mousemovement.addProperty("enabled", keystrokes.get("enabled").getAsBoolean() && keystrokes.get("mousemovement").getAsBoolean());
				convertImageData(keystrokes, mousemovement, "mouseMovementIndicator");
				convertImageData(keystrokes, mousemovement, "mouseMovementIndicatorOuter");
				hud.add("mousemovementhud", mousemovement);
			});
			getObject(hud, "crosshairhud").ifPresent(crosshair ->
				convertImageData(crosshair, crosshair, "customTextureGraphics"));
		});
	}

	private void convertImageData(JsonObject inObj, JsonObject outObj, String name) {
		var graphics = inObj.get(name);
		if (graphics != null && graphics.isJsonObject()) {
			var data = graphics.getAsJsonObject().get("data").getAsJsonArray();
			var pix = GsonHelper.jsonArrayToStream(data).map(JsonElement::getAsJsonArray)
				.map(GsonHelper::jsonArrayToStream)
				.map(s -> s.mapToInt(JsonElement::getAsInt).toArray())
				.toArray(int[][]::new);
			// hacky
			var s = new GraphicsOption("", pix).toSerializedValue();
			outObj.addProperty(name, s);
		} else if (graphics != null) {
			outObj.addProperty(name, graphics.getAsString());
		}
	}
}
