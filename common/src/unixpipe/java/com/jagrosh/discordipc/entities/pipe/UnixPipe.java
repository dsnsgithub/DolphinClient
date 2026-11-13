package com.jagrosh.discordipc.entities.pipe;

/*
 * Replacement for the UnixPipe class of io.github.cdagaming:DiscordIPC.
 *
 * The original implementation connects its IPC pipe through junixsocket
 * (org.newsclub.net.unix), which drags ~6 MB of socket classes, six duplicate
 * multi-release copies and native libraries for sixteen platforms into the
 * client, all for a single call site. This implementation keeps the exact
 * behavior of the original (including its logging, its quirky mkdir warning
 * condition and its single-pass read framing) on the JDK's built-in
 * java.net.Socket facade over a Unix domain SocketChannel, available since
 * Java 16. The client compiles against Java 17, so nothing is lost.
 *
 * Only the four socket-dependent members are re-implemented; the desktop
 * registration helpers are byte-for-byte ports of the original logic.
 */

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.Callback;
import com.jagrosh.discordipc.entities.Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UnixPipe extends Pipe {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnixPipe.class);

	private final Socket socket;

	UnixPipe(IPCClient ipcClient, HashMap<String, Callback> callbacks, File file) throws IOException {
		super(ipcClient, callbacks);
		SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX);
		channel.connect(UnixDomainSocketAddress.of(file.toPath()));
		socket = channel.socket();
	}

	@Override
	public Packet read() throws IOException, JsonParseException {
		InputStream stream = socket.getInputStream();
		while ((status == PipeStatus.CONNECTED || status == PipeStatus.CLOSING) && stream.available() == 0) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException ignored) {
			}
		}
		if (status == PipeStatus.DISCONNECTED) {
			throw new IOException("Disconnected!");
		}
		if (status == PipeStatus.CLOSED) {
			return new Packet(Packet.OpCode.CLOSE, null, ipcClient.getEncoding());
		}

		byte[] bytes = new byte[8];
		int result = stream.read(bytes);
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		if (ipcClient.isDebugMode() && ipcClient.isVerboseLogging()) {
			ipcClient.getCurrentLogger(LOGGER).info(
				String.format("[DEBUG] Read Byte Data: %s with result %s", new String(bytes), result));
		}
		Packet.OpCode op = Packet.OpCode.values()[Integer.reverseBytes(buffer.getInt())];
		bytes = new byte[Integer.reverseBytes(buffer.getInt())];
		result = stream.read(bytes);
		if (ipcClient.isDebugMode() && ipcClient.isVerboseLogging()) {
			ipcClient.getCurrentLogger(LOGGER).info(
				String.format("[DEBUG] Read Reversed Byte Data: %s with result %s", new String(bytes), result));
		}
		return receive(op, bytes);
	}

	@Override
	public void write(byte[] b) throws IOException {
		socket.getOutputStream().write(b);
	}

	@Override
	public void close() throws IOException {
		if (ipcClient.isDebugMode()) {
			ipcClient.getCurrentLogger(LOGGER).info("[DEBUG] Closing IPC pipe...");
		}
		status = PipeStatus.CLOSING;
		send(Packet.OpCode.CLOSE, new JsonObject());
		status = PipeStatus.CLOSED;
		socket.close();
	}

	public boolean mkdir(String path) {
		File dir = new File(path);
		return dir.exists() && dir.isDirectory() || dir.mkdir();
	}

	@Override
	public void registerApp(String applicationId, String currentExecutable) {
		String home = System.getenv("HOME");
		if (home == null) {
			throw new RuntimeException("Unable to find user HOME directory");
		}
		if (currentExecutable == null) {
			try {
				currentExecutable = Files.readSymbolicLink(Paths.get("/proc/self/exe")).toString();
			} catch (Exception e) {
				throw new RuntimeException("Unable to get current exe path from /proc/self/exe", e);
			}
		}
		String content = "[Desktop Entry]\nName=Game" + applicationId + "\nExec=" + currentExecutable
			+ " %%u\nType=Application\nNoDisplay=true\nCategories=Discord;Games;\nMimeType=x-scheme-handler/discord-"
			+ applicationId + ";\n";
		String desktopName = "/discord-" + applicationId + ".desktop";
		String path = home + "/.local";
		if (mkdir(path)) {
			ipcClient.getCurrentLogger(LOGGER).warn(
				"[DEBUG] Failed to create directory '" + path + "', may already exist");
		}
		path = path + "/share";
		if (mkdir(path)) {
			ipcClient.getCurrentLogger(LOGGER).warn(
				"[DEBUG] Failed to create directory '" + path + "', may already exist");
		}
		path = path + "/applications";
		if (mkdir(path)) {
			ipcClient.getCurrentLogger(LOGGER).warn(
				"[DEBUG] Failed to create directory '" + path + "', may already exist");
		}
		path = path + desktopName;
		try (FileWriter writer = new FileWriter(path)) {
			writer.write(content);
		} catch (Exception e) {
			throw new RuntimeException("Failed to write desktop info into '" + path + "'", e);
		}
		String command = "xdg-mime default discord-" + applicationId + ".desktop x-scheme-handler/discord-" + applicationId;
		try {
			ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
			processBuilder.environment();
			int result = processBuilder.start().waitFor();
			if (result < 0) {
				throw new Exception("xdg-mime returned" + result);
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to register mime handler", e);
		}
	}

	@Override
	public void registerSteamGame(String applicationId, String steamId) {
		registerApp(applicationId, "xdg-open steam://rungameid/" + steamId);
	}
}
