package fr.jamailun.stds.client;

import fr.jamailun.stds.common.JavaPacket;

public interface ClientListener {

	void packetReceived(JavaPacket packet);

	void connectionInitializedWithVS();

	void serverDisconnected();

	void serverBroadcastMessage(String message);

}
