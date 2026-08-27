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

package io.github.axolotlclient.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

public class GsonHelper {

	public static final Gson GSON = new GsonBuilder()
		.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
		.registerTypeAdapter(Instant.class, new InstantTypeAdapter())
		.create();

	public static JsonObject fromJson(String s) {
		if (s == null || s.isEmpty()) {
			return new JsonObject();
		}
		return GSON.fromJson(s, JsonObject.class);
	}

	public static Object read(InputStream in) throws IOException {
		try (JsonReader reader = new JsonReader(new InputStreamReader(in))) {
			return read(reader);
		}
	}

	public static Object read(String s) throws IOException {
		try (JsonReader reader = new JsonReader(new StringReader(s))) {
			return read(reader);
		}
	}

	public static Object read(JsonReader reader) throws IOException {
		return switch (reader.peek()) {
			case BEGIN_ARRAY -> {
				List<Object> list = new ArrayList<>();

				reader.beginArray();

				while (reader.hasNext()) {
					list.add(read(reader));
				}

				reader.endArray();

				yield list;
			}
			case BEGIN_OBJECT -> {
				Map<String, Object> object = new LinkedHashMap<>();

				reader.beginObject();

				while (reader.hasNext()) {
					String key = reader.nextName();
					object.put(key, read(reader));
				}

				reader.endObject();

				yield object;
			}
			case STRING -> reader.nextString();
			case NUMBER -> {
				// Ugh.
				String num = reader.nextString();
				try {
					yield Long.parseLong(num);
				} catch (NumberFormatException e) {
					yield Double.parseDouble(num);
				}
			}
			case BOOLEAN -> reader.nextBoolean();
			case NULL -> null;
			// Unused, probably a sign of malformed json
			default -> throw new IllegalStateException();
		};
	}

	public static Stream<JsonElement> jsonArrayToStream(JsonArray array) {
		List<JsonElement> elements = new ArrayList<>(array.size());
		array.forEach(elements::add);
		return elements.stream();
	}
}
