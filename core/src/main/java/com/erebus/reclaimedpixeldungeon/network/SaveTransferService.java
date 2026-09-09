/*
 * Reclaimed Pixel Dungeon
 * Copyright (C) 2026 Erebus
 */

package com.erebus.reclaimedpixeldungeon.network;

import com.badlogic.gdx.files.FileHandle;
import com.erebus.reclaimedpixeldungeon.GamesInProgress;
import com.erebus.reclaimedpixeldungeon.SPDSettings;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.UUID;

public final class SaveTransferService {

	private static final int DISCOVERY_PORT = 38618;
	private static final int TRANSFER_PORT = 38619;
	private static final String BEACON = "RPDSAVE1";
	private static final String REQUEST = "RPDSAVE_REQUEST";
	private static final int MAX_FILE_COUNT = 4096;
	private static final long MAX_FILE_SIZE = 64L * 1024L * 1024L;
	private static final long MAX_TRANSFER_SIZE = 256L * 1024L * 1024L;
	private static final Object LOCK = new Object();
	private static final String deviceId = UUID.randomUUID().toString();

	private static volatile boolean running;
	private static volatile int receiverGeneration;
	private static volatile IncomingRequest incoming;
	private static volatile String notice;
	private static volatile int completedSenderSlot = -1;
	private static ServerSocket serverSocket;
	private static DatagramSocket beaconSocket;

	private SaveTransferService() {}

	public static final class Peer {
		public final String name;
		public final String version;
		public final InetAddress address;
		public final int port;

		private Peer(String name, String version, InetAddress address, int port) {
			this.name = name;
			this.version = version;
			this.address = address;
			this.port = port;
		}
	}

	public static final class IncomingRequest {
		public final String deviceName;
		public final String characterName;
		public final int level;
		private final Socket socket;
		private final DataInputStream input;
		private final DataOutputStream output;

		private IncomingRequest(String deviceName, String characterName, int level,
				Socket socket, DataInputStream input, DataOutputStream output) {
			this.deviceName = deviceName;
			this.characterName = characterName;
			this.level = level;
			this.socket = socket;
			this.input = input;
			this.output = output;
		}
	}

	public static synchronized void startReceiver() {
		if (running) return;
		running = true;
		final int generation = ++receiverGeneration;
		new Thread(() -> {
			ServerSocket localServer = null;
			try {
				localServer = new ServerSocket(TRANSFER_PORT);
				serverSocket = localServer;
				localServer.setSoTimeout(1000);
				while (running && receiverGeneration == generation) {
					try {
						Socket socket = localServer.accept();
						handleRequest(socket);
					} catch (SocketTimeoutException ignored) {
					}
				}
			} catch (IOException e) {
				if (running && receiverGeneration == generation) notice = "Save transfer receiver could not start: " + e.getMessage();
			} finally {
				close(localServer);
			}
		}, "Save Transfer Receiver").start();

		new Thread(() -> {
			DatagramSocket localBeacon = null;
			try {
				localBeacon = new DatagramSocket();
				beaconSocket = localBeacon;
				localBeacon.setBroadcast(true);
				while (running && receiverGeneration == generation) {
					String value = BEACON + "|" + deviceId + "|" + safe(deviceName()) + "|" +
							safe(Game.version) + "|" + TRANSFER_PORT;
					byte[] data = value.getBytes(StandardCharsets.UTF_8);
					for (InetAddress address : broadcastAddresses()) {
						localBeacon.send(new DatagramPacket(data, data.length, address, DISCOVERY_PORT));
					}
					Thread.sleep(250);
				}
			} catch (Exception ignored) {
			} finally {
				if (localBeacon != null) localBeacon.close();
			}
		}, "Save Transfer Beacon").start();
	}

	public static synchronized void stopReceiver() {
		running = false;
		receiverGeneration++;
		close(serverSocket);
		if (beaconSocket != null) beaconSocket.close();
		decline();
	}

	public static ArrayList<Peer> discover(int durationMs) {
		LinkedHashMap<String, Peer> peers = new LinkedHashMap<>();
		try (DatagramSocket socket = new DatagramSocket(null)) {
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(DISCOVERY_PORT));
			socket.setSoTimeout(250);
			long end = System.currentTimeMillis() + durationMs;
			long firstFound = 0;
			byte[] buffer = new byte[1024];
			while (System.currentTimeMillis() < end) {
				try {
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					String[] parts = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8).split("\\|", -1);
					if (parts.length >= 5 && BEACON.equals(parts[0]) && !deviceId.equals(parts[1]) && Game.version.equals(parts[3])) {
						int port = Integer.parseInt(parts[4]);
						peers.put(packet.getAddress().getHostAddress() + ":" + port,
								new Peer(parts[2], parts[3], packet.getAddress(), port));
						if (firstFound == 0) firstFound = System.currentTimeMillis();
					}
				} catch (SocketTimeoutException ignored) {
				}
				if (firstFound > 0 && System.currentTimeMillis() - firstFound >= 750) break;
			}
		} catch (Exception e) {
			notice = "Could not search for receiving devices: " + e.getMessage();
		}
		return new ArrayList<>(peers.values());
	}

	public static void send(final Peer peer, final int slot) {
		new Thread(() -> {
			try (Socket socket = new Socket()) {
				socket.connect(new InetSocketAddress(peer.address, peer.port), 5000);
				socket.setSoTimeout(90000);
				DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
				GamesInProgress.Info info = GamesInProgress.check(slot);
				out.writeUTF(REQUEST);
				out.writeUTF(Game.version);
				out.writeUTF(deviceName());
				out.writeUTF(info.characterName == null || info.characterName.isEmpty() ? info.heroClass.title() : info.characterName);
				out.writeInt(info.level);
				out.flush();
				String response = in.readUTF();
				if (!"ACCEPT".equals(response)) {
					notice = "The save transfer was declined.";
					return;
				}
				sendDirectory(out, slot);
				out.flush();
				if (in.readBoolean()) {
					completedSenderSlot = slot;
					notice = "Save transfer completed. The sender's local save was removed.";
				} else {
					notice = "The receiving device could not validate the save.";
				}
			} catch (Exception e) {
				notice = "Save transfer failed: " + e.getMessage();
			}
		}, "Save Transfer Sender").start();
	}

	public static IncomingRequest consumeIncoming() {
		synchronized (LOCK) {
			IncomingRequest result = incoming;
			incoming = null;
			return result;
		}
	}

	public static String consumeNotice() {
		String result = notice;
		notice = null;
		return result;
	}

	public static int consumeCompletedSenderSlot() {
		int result = completedSenderSlot;
		completedSenderSlot = -1;
		return result;
	}

	public static void decline() {
		IncomingRequest request;
		synchronized (LOCK) {
			request = incoming;
			incoming = null;
		}
		decline(request);
	}

	public static void decline(IncomingRequest request) {
		if (request == null) return;
		try { request.output.writeUTF("DECLINE"); request.output.flush(); } catch (IOException ignored) {}
		close(request.socket);
	}

	public static void accept(final IncomingRequest request, final int slot) {
		new Thread(() -> {
			boolean valid = false;
			String temp = "save-transfer-" + UUID.randomUUID();
			try {
				request.output.writeUTF("ACCEPT");
				request.output.flush();
				receiveDirectory(request.input, temp);
				if (!GamesInProgress.validateTransferredGame(temp + "/game.dat", slot)) {
					throw new IOException("The temporary save package could not be previewed.");
				}
				if (GamesInProgress.gameExists(slot)) throw new IOException("The selected slot is no longer empty.");
				// Erased saves leave a one-byte game.dat marker behind. The slot is empty,
				// but its directory must be removed or moveTo nests the transfer inside it.
				if (FileUtils.dirExists(GamesInProgress.gameFolder(slot))
						&& !FileUtils.deleteDir(GamesInProgress.gameFolder(slot))) {
					throw new IOException("The empty destination slot could not be prepared.");
				}
				String destination = GamesInProgress.gameFolder(slot);
				for (String name : FileUtils.filesInDir(temp)) {
					if (!validName(name)) throw new IOException("Unsafe temporary save filename.");
					FileHandle sourceFile = FileUtils.getFileHandle(temp + "/" + name);
					FileUtils.getFileHandle(destination + "/" + name).writeBytes(sourceFile.readBytes(), false);
				}
				GamesInProgress.setUnknown(slot);
				valid = FileUtils.fileLength(GamesInProgress.gameFile(slot)) > 1;
				if (!valid) FileUtils.deleteDir(GamesInProgress.gameFolder(slot));
				request.output.writeBoolean(valid);
				request.output.flush();
				notice = valid ? "Received " + request.characterName + " successfully." : "The received save was not valid.";
			} catch (Exception e) {
				notice = "Save transfer failed: " + e.getMessage();
				try { request.output.writeBoolean(false); request.output.flush(); } catch (IOException ignored) {}
			} finally {
				FileUtils.deleteDir(temp);
				close(request.socket);
			}
		}, "Save Transfer Receiver Install").start();
	}

	private static void handleRequest(Socket socket) {
		try {
			socket.setSoTimeout(90000);
			DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
			if (!REQUEST.equals(in.readUTF())) throw new IOException("Unknown request");
			String version = in.readUTF();
			String sender = in.readUTF();
			String character = in.readUTF();
			int level = in.readInt();
			if (!Game.version.equals(version) || GamesInProgress.firstEmpty() == -1) {
				out.writeUTF("DECLINE"); out.flush(); close(socket); return;
			}
			synchronized (LOCK) {
				if (incoming != null) { out.writeUTF("DECLINE"); out.flush(); close(socket); return; }
				incoming = new IncomingRequest(sender, character, level, socket, in, out);
			}
		} catch (Exception e) {
			close(socket);
		}
	}

	private static void sendDirectory(DataOutputStream out, int slot) throws Exception {
		String folder = GamesInProgress.gameFolder(slot);
		ArrayList<String> files = FileUtils.filesInDir(folder);
		out.writeInt(files.size());
		for (String name : files) {
			if (!validName(name)) throw new IOException("Unsafe save filename");
			FileHandle file = FileUtils.getFileHandle(folder + "/" + name);
			byte[] data = file.readBytes();
			out.writeUTF(name);
			out.writeLong(data.length);
			out.write(digest(data));
			out.write(data);
		}
	}

	private static void receiveDirectory(DataInputStream in, String folder) throws Exception {
		int count = in.readInt();
		if (count < 1 || count > MAX_FILE_COUNT) throw new IOException("Invalid file count");
		long total = 0;
		boolean gameFile = false;
		for (int i = 0; i < count; i++) {
			String name = in.readUTF();
			long length = in.readLong();
			byte[] expected = new byte[32];
			in.readFully(expected);
			if (!validName(name) || length < 1 || length > MAX_FILE_SIZE || (total += length) > MAX_TRANSFER_SIZE) throw new IOException("Invalid save package");
			byte[] data = new byte[(int)length];
			in.readFully(data);
			if (!MessageDigest.isEqual(expected, digest(data))) throw new IOException("Save checksum mismatch");
			FileUtils.getFileHandle(folder + "/" + name).writeBytes(data, false);
			if ("game.dat".equals(name)) gameFile = true;
		}
		if (!gameFile) throw new IOException("Save package has no game.dat");
	}

	private static byte[] digest(byte[] data) throws Exception {
		return MessageDigest.getInstance("SHA-256").digest(data);
	}

	private static boolean validName(String name) {
		return name != null && !name.isEmpty() && !name.contains("/") && !name.contains("\\") && !name.contains("..") && !name.endsWith(".spdtmp");
	}

	private static String deviceName() {
		String configured = SPDSettings.saveTransferDeviceName();
		if (!configured.isEmpty()) return configured;
		String name = System.getProperty("user.name");
		if (name == null || name.trim().isEmpty()) name = System.getProperty("os.name");
		return name == null || name.trim().isEmpty() ? "Reclaimed Pixel Dungeon" : name.trim();
	}

	private static String safe(String value) {
		return value == null ? "" : value.replace("|", " ").replace("\n", " ");
	}

	private static ArrayList<InetAddress> broadcastAddresses() throws Exception {
		ArrayList<InetAddress> result = new ArrayList<>();
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements()) {
			NetworkInterface network = interfaces.nextElement();
			if (!network.isUp() || network.isLoopback()) continue;
			for (InterfaceAddress address : network.getInterfaceAddresses()) if (address.getBroadcast() != null) result.add(address.getBroadcast());
		}
		if (result.isEmpty()) result.add(InetAddress.getByName("255.255.255.255"));
		return result;
	}

	private static void close(ServerSocket socket) { if (socket != null) try { socket.close(); } catch (IOException ignored) {} }
	private static void close(Socket socket) { if (socket != null) try { socket.close(); } catch (IOException ignored) {} }
}
