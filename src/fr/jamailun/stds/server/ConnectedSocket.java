package fr.jamailun.stds.server;

import fr.jamailun.stds.common.ClientInfo;
import fr.jamailun.stds.common.JavaPacket;
import fr.jamailun.stds.common.standard.DisconnectionPacket;
import fr.jamailun.stds.common.standard.HandshakePacket;
import fr.jamailun.stds.common.standard.RenamePacket;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ConnectedSocket {

	private ClientInfo clientInfo;
	private final Socket client;
	private final VirtualServer virtualServer;
	private ObjectOutputStream oos;
	ConnectedSocket(Socket socket, VirtualServer virtualServer)  {
		this.client = socket;
		clientInfo = new ClientInfo(client.toString());
		try {
			client.setKeepAlive(true);
			oos = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.virtualServer = virtualServer;
		startListening();
	}

	public Socket getSocket() {
		return client;
	}

	public ClientInfo getClientInfo() {
		return clientInfo;
	}

	void sendPacket(JavaPacket packet) {
		try {
			oos.writeObject(packet);
			oos.flush();
		} catch (SocketException se) {
			disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startListening() {
		new Thread(() -> {
			try(ObjectInputStream ois = new ObjectInputStream(client.getInputStream())) {
				while(true) {
					try {Thread.sleep(100);} catch (InterruptedException es) {es.printStackTrace();}
					try {
						Object object = ois.readObject();
						if (object == null)
							continue;
						if (!(object instanceof JavaPacket)) {
							JavaServer.error("Could not parse object " + object + ".");
							continue;
						}
						packetReceived((JavaPacket) object);
					} catch(StreamCorruptedException ste) {
						disconnect();
						return;
					} catch (EOFException eof) {
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (ClassNotFoundException e1) {
						JavaServer.error("Unknown class : from client " + client + ". ->" + e1.getMessage());
					} catch (SocketException se) {
						if(client.isClosed() || se.getMessage().equals("Connection reset")) {
							disconnect();
							return;
						}
						se.printStackTrace();
					} catch (IOException e2) {
						e2.printStackTrace();
						if( client.isClosed() || !client.isConnected() || ! client.isBound() || e2.getMessage().equals("Connection reset") ) {
							disconnect();
							return;
						}
						JavaServer.error("Error while getting object from client " + client+". ->" + e2.getMessage());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void disconnect() {
		try {
			client.close();
		} catch (IOException ignored) {}
		virtualServer.disconnect(this);
	}

	private void packetReceived(JavaPacket packet) {
		if(packet instanceof HandshakePacket) {
			virtualServer.handShake(this, ((HandshakePacket)packet).getClientName());
			return;
		}

		if(packet instanceof DisconnectionPacket) {
			disconnect();
			return;
		}

		if(packet instanceof RenamePacket) {
			virtualServer.renameClient(this, ((RenamePacket)packet).getName());
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {e.printStackTrace();}
			return;
		}

		virtualServer.packetReceived(packet, this);
	}

	public void rename(String name) {
		clientInfo = clientInfo.rename(name);
	}
}