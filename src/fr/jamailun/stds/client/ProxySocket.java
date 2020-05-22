package fr.jamailun.stds.client;

import fr.jamailun.stds.common.JavaPacket;
import fr.jamailun.stds.common.ServerInfo;
import fr.jamailun.stds.common.standard.DisconnectionPacket;
import fr.jamailun.stds.common.standard.HandshakePacket;
import fr.jamailun.stds.common.standard.ServerBroadcastPacket;
import fr.jamailun.stds.common.standard.ServerResponsePacket;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

public class ProxySocket extends Thread {

	protected final Socket server;
	protected final ObjectOutputStream oos;
	protected final ClientListener listener;
	private ClientStep step;
	ProxySocket(String address, ServerInfo serverInfo, ClientListener listener) throws IOException {
		step = ClientStep.CONNECT_TO_VS;
		server = new Socket(address, serverInfo.getPort());
		this.listener = listener;
		server.setKeepAlive(true);
		oos = new ObjectOutputStream(server.getOutputStream());
	}

	@Override
	public void run() {
		step = ClientStep.SEND_HANDSHAKE;
		sendPacket(new HandshakePacket("client_"+new Random(server.getLocalPort()).nextInt(10000)));
		step = ClientStep.WAIT_ANSWER_HANDSHAKE;
		startListening();
	}

	public void sendPacket(JavaPacket packet) {
		try {
			JavaClient.log("Sending packet : " + packet+".");
			oos.writeObject(packet);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void startListening() {
		new Thread(() -> {
			try(ObjectInputStream ois = new ObjectInputStream(server.getInputStream())) {
				while(true) {
					try {Thread.sleep(100);} catch (InterruptedException es) {es.printStackTrace();}
					try {
						Object object = null;
						try {
							object = ois.readObject();
						} catch (EOFException eof) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						if(object == null)
							continue;
						if( ! (object instanceof  JavaPacket)) {
							JavaClient.error("Could not parse object " + object+".");
							continue;
						}
						packetReceived((JavaPacket)object);
					} catch (ClassNotFoundException e1) {
						JavaClient.error("Unknown class : from server " + server+". ->" + e1.getMessage());
					} catch (IOException e2) {
						if( server.isClosed() || ! server.isConnected() || ! server.isBound() ) {
							JavaClient.error("Server aborted connection.");
							return;
						}
						JavaClient.error("Error while getting object from server " + server+". ->" + e2.getMessage());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();
	}

	private void packetReceived(JavaPacket packet) {
		if(step == ClientStep.WAIT_ANSWER_HANDSHAKE) {
			if( ! (packet instanceof ServerResponsePacket)) {
				JavaClient.error("Received packet " + packet + ", but waiting for handshake answer !");
				return;
			}
			int r = ((ServerResponsePacket)packet).getAnswer();
			switch (r) {
				case ServerResponsePacket.ACCEPTED:
					JavaClient.log("Fully connected to server from now !");
					step = ClientStep.CONNECTED;
					if(listener != null)
						listener.connectionInitializedWithVS();
					return;
				case ServerResponsePacket.NAME_TAKEN:
					JavaClient.error("Could not connect to server : NAME TAKEN.");
					return;
				case ServerResponsePacket.SERVER_FULL:
					JavaClient.error("Could not connect to server : SERVER FULL.");
					return;
				default:
					JavaClient.error("Unknown response value : " + r+".");
					return;
			}
		}
		if(step == ClientStep.CONNECTED) {
			if(packet instanceof  DisconnectionPacket) {
				step = ClientStep.DISCONNECTING;
				if(listener != null)
					listener.serverDisconnected();
				else
					JavaClient.error("Server stopped the connection properly.");
				return;
			}

			if(packet instanceof ServerBroadcastPacket) {
				String broadcast = ((ServerBroadcastPacket)packet).getMessage();
				if(listener != null)
					listener.serverBroadcastMessage(broadcast);
				else
					JavaClient.log("[BROADCAST] "+broadcast);
				return;
			}
		}

		if(listener != null)
			listener.packetReceived(packet);
		else
			JavaClient.warn("Packet received : " + packet +". Please, configure a listener before starting the client.");
	}

}