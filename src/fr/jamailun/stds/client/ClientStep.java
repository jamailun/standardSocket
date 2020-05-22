package fr.jamailun.stds.client;

public enum ClientStep {
	CONNECT_TO_VS,
	SEND_HANDSHAKE,
	WAIT_ANSWER_HANDSHAKE,
	CONNECTED,
	DISCONNECTING;
}
