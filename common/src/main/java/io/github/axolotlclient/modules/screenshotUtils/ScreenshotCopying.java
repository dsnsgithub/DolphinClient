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

package io.github.axolotlclient.modules.screenshotUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import io.github.axolotlclient.DolphinClientCommon;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.sdl.SDLClipboard;
import org.lwjgl.sdl.SDL_ClipboardCleanupCallback;
import org.lwjgl.sdl.SDL_ClipboardDataCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

@UtilityClass
public class ScreenshotCopying {

	static {
		boolean sdl = false;
		try {
			Class.forName("org.lwjgl.sdl.SDL");
			sdl = true;
		} catch (Throwable ignored) {
		}
		SDL_AVAILABLE = sdl;
		boolean wayland = false;
		if (!sdl) {
			try {
				wayland = GLFW.glfwGetPlatform() == GLFW.GLFW_PLATFORM_WAYLAND || System.getenv("WAYLAND_DISPLAY") != null;
			} catch (Throwable ignored) {
			}
		}
		IS_WAYLAND = wayland;
	}

	private static final boolean SDL_AVAILABLE;
	private static final boolean IS_WAYLAND;

	public void copy(Path file) {
		if (!SDL_AVAILABLE || !copySdl(file)) {
			if (IS_WAYLAND) {
				copyWayland(file);
			} else {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new FileTransferable(file.toFile()), null);
			}
		}
	}

	private void copyWayland(Path f) {
		try {
			ProcessBuilder builder = new ProcessBuilder("bash", "-c", "wl-copy -t image/png < '" + f.toAbsolutePath() + "'");
			Process p = builder.start();
			p.waitFor();
		} catch (IOException | InterruptedException ignored) {
			DolphinClientCommon.getInstance().getLogger().error("Failed to invoke 'wl-copy'!\nMake sure 'wl-clipboard' is installed and accessible!");
		}
	}

	public void copy(byte[] image) {
		if (!SDL_AVAILABLE || !SDLFence.copySdl(image)) {
			if (IS_WAYLAND) {
				try {
					Path i = Files.createTempFile("axolotlclient_screenshot", ".png");
					Files.write(i, image);
					copyWayland(i);
					Files.delete(i);
				} catch (IOException e) {
					DolphinClientCommon.getInstance().getLogger().error("Failed to copy image using temporary file!");
				}
			} else {
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new ImageTransferable(image), null);
			}
		}
	}

	private boolean copySdl(Path p) {
		try {
			return SDLFence.copySdl(Files.readAllBytes(p));
		} catch (IOException e) {
			DolphinClientCommon.getInstance().getLogger().error("Failed to read screenshot!", e);
		}
		return false;
	}

	// Classloading fence...
	static class SDLFence {
		private static boolean copySdl(byte[] data) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				var mimetypesBuf = stack.UTF8("image/png");
				var mimetypes = stack.pointers(mimetypesBuf);
				var dataBuf = ByteBuffer.allocateDirect(data.length).put(data).flip();
				var pointer = MemoryUtil.memAddress(dataBuf);
				return SDLClipboard.SDL_SetClipboardData(SDL_ClipboardDataCallback.create((userdata, mime_type, size) -> {
						var sizeBuf = MemoryUtil.memLongBuffer(size, 8);
						sizeBuf.put(0, data.length);
						return userdata;
					}),
					SDL_ClipboardCleanupCallback.create(userdata -> MemoryUtil.memFree(dataBuf)), pointer, mimetypes);
			}
		}
	}

	protected record ImageTransferable(byte[] image) implements Transferable {
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.imageFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.imageFlavor.equals(flavor);
		}

		@NotNull
		@Override
		public Object getTransferData(DataFlavor flavor) throws IOException {
			return ImageIO.read(new ByteArrayInputStream(image));
		}
	}

	protected record FileTransferable(File file) implements Transferable {
		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return new DataFlavor[]{DataFlavor.javaFileListFlavor};
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return DataFlavor.javaFileListFlavor.equals(flavor);
		}

		@Override
		public @NotNull Object getTransferData(DataFlavor flavor) {
			final ArrayList<File> files = new ArrayList<>();
			files.add(file);
			return files;
		}
	}
}
