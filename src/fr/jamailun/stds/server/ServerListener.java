package fr.jamailun.stds.server;

import fr.jamailun.stds.common.JavaPacket;

public interface ServerListener {

	void packetReceived(JavaPacket packet, ConnectedSocket client);

	void clientDisconnected(ConnectedSocket client);

	void clientConnected(ConnectedSocket client);

	void virtualRoomShutdowns();

}