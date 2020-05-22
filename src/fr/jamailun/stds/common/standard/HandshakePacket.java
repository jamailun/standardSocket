package fr.jamailun.stds.common.standard;

import fr.jamailun.stds.common.JavaPacket;

public class HandshakePacket extends JavaPacket {
	private static final long serialVersionUID = 501L;
	private final String clientName;
	public HandshakePacket(String clientName) {
		super("handshake");
		this.clientName = clientName;
	}

	public String getClientName() {
		return clientName;
	}
}
