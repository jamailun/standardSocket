package fr.jamailun.stds.server;

import fr.jamailun.stds.common.ClientInfo;
import fr.jamailun.stds.common.JavaPacket;
import fr.jamailun.stds.common.standard.DisconnectionPacket;
import fr.jamailun.stds.common.standard.ServerResponsePacket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class VirtualServer extends Thread {

	private Set<ServerListener> recipes;

	private int maxConnected;
	private String name;
	private Set<ConnectedSocket> clients;
	private final int port;

	private final JavaServer main;
	VirtualServer(JavaServer main, String name, int port, int maxConnected) {
		this.main = main;
		this.maxConnected = maxConnected;
		this.name = name;
		this.port = port;
		clients = new CopyOnWriteArraySet<>();
		recipes = new CopyOnWriteArraySet<>();
	}

	/**
	 * Call this method to register a ServerListener.
	 * @param listener ServerListener to be added to recipes list.
	 */
	public void registerListener(ServerListener listener) {
		recipes.add(listener);
	}

	public void unregisterListener(ServerListener listener) {
		recipes.remove(listener);
	}

	public Set<ServerListener> getAllListeners() {
		return new HashSet<>(recipes);
	}

	public int getMaxConnected() {
		return maxConnected;
	}

	public int getPort() {
		return port;
	}

	public void rename(String name) {
		this.name = name;
	}

	public String getServerName() {
		return name;
	}

	public Set<ClientInfo> getClientsInfo() {
		return clients.stream().map(ConnectedSocket::getClientInfo).collect(Collectors.toSet());
	}

	public void connect(Socket client) {
		if(isConnected(client))
			throw new IllegalArgumentException("Client already connected ! " + client+".");
		ConnectedSocket connectedSocket = new ConnectedSocket(client, this);
		clients.add(connectedSocket);
		recipes.forEach(r -> r.clientConnected(connectedSocket));
		//sendPacketToClient(new ClientListPacket(clients.stream().map(ConnectedSocket::getClientInfo).collect(Collectors.toList())), connectedSocket);
	}

	public int getConnected() {
		return clients.size();
	}

	private ServerSocket serverSocket;
	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			JavaServer.log("Virtual server ["+name+"] is now listening on port " + port+".");
			while(main.isRunning() && !cancel) {
				try {
					Socket client = serverSocket.accept();
					connect(client);
				} catch (IOException ee) {
					if(cancel)
						return;
					JavaServer.error("VS["+name+"] Could not accept socket : " +ee.getMessage());
				}
			}
			serverSocket.close();
		} catch (IOException e) {
			if( ! cancel)
				JavaServer.error("VS["+name+"] Couldn't listen on port " + port+" : " + e.getMessage());
		}
	}

	public void disconnect(ConnectedSocket connectedSocket) {
		clients.remove(connectedSocket);
		recipes.forEach(r -> r.clientDisconnected(connectedSocket));
	}

	void packetReceived(JavaPacket packet, ConnectedSocket client) {
		recipes.forEach(r -> r.packetReceived(packet, client));
	}

	public void sendPacketToClients(JavaPacket packet, ConnectedSocket client, boolean sendToItself) {
		for(ConnectedSocket cl : clients) {
			if(sendToItself || ! cl.equals(client))
				cl.sendPacket(packet);
		}
	}

	public void sendPacketToClients(JavaPacket packet) {
		for(ConnectedSocket cl : clients) {
			cl.sendPacket(packet);
		}
	}

	private boolean isConnected(Socket socket) {
		for(ConnectedSocket cs : clients)
			if(cs.getSocket().equals(socket))
				return true;
			return false;
	}

	public void sendPacketToClient(JavaPacket packet, ConnectedSocket client) {
		if(!isConnected(client.getSocket())) {
			JavaServer.error("This client is not connected ! " + client + ".");
			disconnect(client);
		}
		client.sendPacket(packet);
	}

	void renameClient(ConnectedSocket client, String name) {
		for(ConnectedSocket cs : clients) {
			if (!cs.getSocket().equals(client.getSocket())) {
				if (cs.getClientInfo().getName().equals(name)) {
					sendPacketToClient(new ServerResponsePacket(ServerResponsePacket.NAME_TAKEN), client);
					return;
				}
			}
		}
		sendPacketToClient(new ServerResponsePacket(ServerResponsePacket.ACCEPTED), client);
		client.rename(name);
	}

	public void handShake(ConnectedSocket client, String name) {
		if(clients.size() >= maxConnected) {
			sendPacketToClient(new ServerResponsePacket(ServerResponsePacket.SERVER_FULL), client);
			disconnect(client);
			return;
		}
		for(ConnectedSocket cs : clients) {
			if (!cs.getSocket().equals(client.getSocket())) {
				if (cs.getClientInfo().getName().equals(name)) {
					sendPacketToClient(new ServerResponsePacket(ServerResponsePacket.NAME_TAKEN), client);
					return;
				}
			}
		}
		sendPacketToClient(new ServerResponsePacket(ServerResponsePacket.ACCEPTED), client);
		client.rename(name);
	}

	private boolean cancel = false;
	public void shutdown() {
		recipes.forEach(ServerListener::virtualRoomShutdowns);
		sendPacketToClients(new DisconnectionPacket());
		clients.forEach(cl -> {
			recipes.forEach(r -> r.clientDisconnected(cl));
			try {
				cl.getSocket().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		clients.clear();
		recipes.clear();
		try {
			cancel = true;
			serverSocket.close();
		} catch (IOException e) {
			System.err.println("Could not close serverSocket on VS(port="+port+") : " + e.getMessage()+".");
		}
		JavaServer.log("VS ("+name+") shutdown finished.");
	}
}