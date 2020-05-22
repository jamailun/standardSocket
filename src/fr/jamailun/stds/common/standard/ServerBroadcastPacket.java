package fr.jamailun.stds.common.standard;

import fr.jamailun.stds.common.JavaPacket;

public class ServerBroadcastPacket extends JavaPacket {
	private static final long serialVersionUID = 57846501L;
	private final String message;
	public ServerBroadcastPacket(String message) {
		super("serverBroadcast");
		this.message = message;
	}
	public String getMessage() {
		return message;
	}
}
