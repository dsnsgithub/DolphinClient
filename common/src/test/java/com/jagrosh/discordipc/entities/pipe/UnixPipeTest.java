package com.jagrosh.discordipc.entities.pipe;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jagrosh.discordipc.IPCClient;
import com.jagrosh.discordipc.entities.Packet;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the JDK-socket UnixPipe replacement speaks the exact same wire
 * protocol as the junixsocket-based original: length-prefixed little-endian
 * framing, blocking behavior when no data is available yet, and the close
 * handshake.
 */
class UnixPipeTest {

	@Test
	@EnabledOnOs({OS.LINUX, OS.MAC})
	@Timeout(20)
	void roundTrip() throws Exception {
		Assumptions.assumeTrue(Runtime.version().feature() >= 17, "requires Unix domain socket support");
		Path socketPath = Files.createTempDirectory("dolphinclient-ipc-test").resolve("discord-ipc-0");
		ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.UNIX);
		server.bind(UnixDomainSocketAddress.of(socketPath));

		CompletableFuture<byte[]> writtenByPipe = new CompletableFuture<>();
		CompletableFuture<ByteBuffer> serverSent = new CompletableFuture<>();
		CompletableFuture<Packet.OpCode> closeOp = new CompletableFuture<>();

		Thread serverThread = new Thread(() -> {
			try (SocketChannel client = server.accept()) {
				// 1. push a frame to the client, slightly delayed so read() exercises
				//    its available() busy-wait instead of seeing data immediately
				Thread.sleep(300);
				JsonObject json = new JsonObject();
				json.addProperty("type", "test");
				ByteBuffer frame = ByteBuffer.wrap(new Packet(Packet.OpCode.FRAME, json).toBytes());
				while (frame.hasRemaining()) {
					client.write(frame);
				}
				serverSent.complete(frame.duplicate().flip());

				// 2. read what the pipe writes back
				byte[] written = new byte[64];
				InputStream in = client.socket().getInputStream();
				int total = in.read(written);
				writtenByPipe.complete(java.util.Arrays.copyOf(written, total));

				// 3. observe the close handshake
				byte[] header = new byte[8];
				readFully(in, header);
				ByteBuffer headerBuffer = ByteBuffer.wrap(header);
				closeOp.complete(Packet.OpCode.values()[Integer.reverseBytes(headerBuffer.getInt())]);
				int payloadLength = Integer.reverseBytes(headerBuffer.getInt());
				byte[] payload = new byte[payloadLength];
				if (payloadLength > 0) {
					readFully(in, payload);
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
		serverThread.setDaemon(true);
		serverThread.start();

		IPCClient client = new IPCClient(1234567890L);
		UnixPipe pipe = new UnixPipe(client, new HashMap<>(), socketPath.toFile());
		assertTrue(pipe.status == PipeStatus.CONNECTING);
		pipe.status = PipeStatus.CONNECTED;

		// 1. frame arrives despite the delay (proves available() works on the
		//    JDK socket facade and the framing matches the original)
		Packet received = pipe.read();
		assertSame(Packet.OpCode.FRAME, received.getOp());
		assertEquals("test", received.getJson().get("type").getAsString());

		// 2. writing round-trips untouched
		byte[] payload = "payload-ping".getBytes(StandardCharsets.UTF_8);
		pipe.write(payload);
		assertArrayEquals(payload, writtenByPipe.get(5, TimeUnit.SECONDS));

		// 3. close() sends the CLOSE frame over the pipe before closing
		pipe.close();
		assertSame(Packet.OpCode.CLOSE, closeOp.get(5, TimeUnit.SECONDS));
		assertTrue(pipe.status == PipeStatus.CLOSED);

		// after close(), read() returns a CLOSE packet without touching the socket
		Packet afterClose = pipe.read();
		assertSame(Packet.OpCode.CLOSE, afterClose.getOp());
		assertNull(afterClose.getJson());
	}

	private static void readFully(InputStream in, byte[] buffer) throws Exception {
		int offset = 0;
		while (offset < buffer.length) {
			int read = in.read(buffer, offset, buffer.length - offset);
			if (read < 0) {
				throw new IllegalStateException("unexpected end of stream");
			}
			offset += read;
		}
	}
}
