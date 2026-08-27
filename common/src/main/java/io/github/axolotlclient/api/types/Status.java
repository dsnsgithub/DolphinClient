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

package io.github.axolotlclient.api.types;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import io.github.axolotlclient.DolphinClientCommon;
import io.github.axolotlclient.bridge.util.AxoI18n;
import io.github.axolotlclient.util.GsonHelper;
import lombok.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
@EqualsAndHashCode
@ToString
@AllArgsConstructor
public class Status {

	public static final Status UNKNOWN = new Status("offline", null, Activity.UNKNOWN);

	private String type;
	@Nullable
	private final Instant lastOnline;
	@Nullable
	private Activity activity;

	public boolean isOnline() {
		return "online".equals(type);
	}

	public String getDescription() {
		return activity == null || activity.description.isEmpty() ? "" : AxoI18n.translate(activity.description);
	}

	public String getTitle() {
		if (!isOnline()) {
			return AxoI18n.translate("api.status.title.offline");
		}
		return AxoI18n.translate(activity == null || activity.title.isEmpty() ? "api.status.title.online" : activity.title);
	}

	public String getLastOnline() {
		return lastOnline == null ? null : AxoI18n.translate("api.status.last_online", lastOnline.atZone(ZoneId.systemDefault()).format(DolphinClientCommon.getInstance().getConfig().getDateTimeFormatter()));
	}

	public void setOnline(boolean online) {
		if (online) {
			type = "online";
		} else {
			type = "offline";
		}
	}

	public record Activity(String title, String description, Metadata metadata,
						   @EqualsAndHashCode.Exclude Instant started) {
		private static final Activity UNKNOWN = new Activity("", "", null, Instant.EPOCH);

		public Activity(String title, String description) {
			this(title, description, (Metadata) null);
		}

		public Activity(String title, String description, Metadata metadata) {
			this(title, description, metadata, Instant.now());
		}

		public Activity(String title, String description, MetadataAttributes attributes) {
			this(title, description, attributes != null ? new Metadata(attributes) : null);
		}

		public boolean hasMetadata() {
			return metadata != null;
		}

		public boolean hasMetadata(String id) {
			return hasMetadata() && metadata.type.equals(id);
		}

		public interface MetadataAttributes {
			String typeId();
		}

		@JsonAdapter(Metadata.MetadataTypeAdapter.class)
		public record Metadata(String type, MetadataAttributes attributes) {
			public Metadata(MetadataAttributes attributes) {
				this(attributes.typeId(), attributes);
			}

			public static class MetadataTypeAdapter extends TypeAdapter<Metadata> {

				@Override
				public void write(JsonWriter out, Metadata value) throws IOException {
					if (value == null) {
						out.nullValue();
						return;
					}
					out.beginObject();
					out.name("type").value(value.type);
					out.name("attributes").jsonValue(GsonHelper.GSON.toJson(value.attributes));
					out.endObject();
				}

				@Override
				public Metadata read(JsonReader in) throws IOException {
					if (in.peek() == JsonToken.NULL) {
						return null;
					}
					JsonObject metadataObj = GsonHelper.GSON.fromJson(in, JsonObject.class);
					String type = metadataObj.get("type").getAsString();
					MetadataAttributes attributes = switch (type) {
						case WorldHostMetadata.ID ->
							GsonHelper.GSON.fromJson(metadataObj.get("attributes"), WorldHostMetadata.class);
						case E4mcMetadata.ID ->
							GsonHelper.GSON.fromJson(metadataObj.get("attributes"), E4mcMetadata.class);
						case ExternalServerMetadata.ID ->
							GsonHelper.GSON.fromJson(metadataObj.get("attributes"), ExternalServerMetadata.class);
						default -> throw new IllegalArgumentException("Unsupported attributes id: " + type);
					};
					return new Metadata(type, attributes);
				}
			}
		}

		public record WorldHostMetadata(String connectionId, String externalIp,
										ServerInfo serverInfo) implements MetadataAttributes {
			public static final String ID = "world_host";

			@Override
			public String typeId() {
				return ID;
			}

			public E4mcMetadata asE4mcMetadata() {
				return new E4mcMetadata(externalIp, serverInfo);
			}
		}

		public record E4mcMetadata(String domain, ServerInfo serverInfo) implements MetadataAttributes {
			public static final String ID = "e4mc";

			@Override
			public String typeId() {
				return ID;
			}
		}

		public record ExternalServerMetadata(String serverName, String address) implements MetadataAttributes {
			public static final String ID = "external_server";

			@Override
			public String typeId() {
				return ID;
			}
		}

		public record ServerInfo(String levelName, String description, Favicon icon, Players players, Version version) {
			@JsonAdapter(Favicon.FaviconTypeAdapter.class)
			public record Favicon(byte[] iconBytes) {
				private static final String PREFIX = "data:image/png;base64,";

				@Override
				public @NotNull String toString() {
					return PREFIX + new String(Base64.getEncoder().encode(iconBytes), StandardCharsets.UTF_8);
				}

				public static Favicon fromString(String base64String) {
					if (!base64String.startsWith(PREFIX)) {
						return new Favicon(null);
					}
					return new Favicon(Base64.getDecoder().decode(base64String.substring(PREFIX.length()).replaceAll("\n", "").getBytes(StandardCharsets.UTF_8)));
				}

				public static class FaviconTypeAdapter extends TypeAdapter<Favicon> {

					@Override
					public void write(JsonWriter out, Favicon value) throws IOException {
						if (value.iconBytes() == null) {
							out.nullValue();
							return;
						}
						out.value(value.toString());
					}

					@Override
					public Favicon read(JsonReader in) throws IOException {
						return fromString(in.nextString());
					}
				}
			}

			public record Players(int max, int online, List<Player> sample) {
				public record Player(String name, String uuid) {
				}
			}

			public record Version(String name, int protocol) {
			}
		}
	}
}
