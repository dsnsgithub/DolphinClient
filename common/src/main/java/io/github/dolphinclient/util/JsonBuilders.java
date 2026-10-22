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

package io.github.dolphinclient.util;

import com.google.gson.JsonElement;

public final class JsonBuilders {
	public static final class JsonArray {
		private final com.google.gson.JsonArray arr = new com.google.gson.JsonArray();

		public static JsonArray create() {
			return new JsonArray();
		}

		private JsonArray() {
		}

		public JsonArray field(JsonElement ele) {
			arr.add(ele);
			return this;
		}

		public JsonArray field(JsonArray builder) {
			return field(builder.build());
		}

		public JsonArray field(JsonObject builder) {
			return field(builder.build());
		}

		public JsonArray field(Number prop) {
			arr.add(prop);
			return this;
		}

		public JsonArray field(String prop) {
			arr.add(prop);
			return this;
		}

		public JsonArray field(Boolean prop) {
			arr.add(prop);
			return this;
		}

		public JsonArray field(Character prop) {
			arr.add(prop);
			return this;
		}

		public com.google.gson.JsonArray build() {
			return arr;
		}

		public String asString() {
			return arr.toString();
		}
	}

	public static final class JsonObject {

		private final com.google.gson.JsonObject obj = new com.google.gson.JsonObject();

		public static JsonObject create() {
			return new JsonObject();
		}

		private JsonObject() {
		}

		public JsonObject field(String name, JsonElement ele) {
			obj.add(name, ele);
			return this;
		}

		public JsonObject field(String name, JsonObject builder) {
			return field(name, builder.build());
		}

		public JsonObject field(String name, JsonArray builder) {
			return field(name, builder.build());
		}

		public JsonObject field(String name, Number prop) {
			obj.addProperty(name, prop);
			return this;
		}

		public JsonObject field(String name, String prop) {
			obj.addProperty(name, prop);
			return this;
		}

		public JsonObject field(String name, Boolean prop) {
			obj.addProperty(name, prop);
			return this;
		}

		public JsonObject field(String name, Character prop) {
			obj.addProperty(name, prop);
			return this;
		}

		public com.google.gson.JsonObject build() {
			return obj;
		}

		public String asString() {
			return obj.toString();
		}
	}
}
