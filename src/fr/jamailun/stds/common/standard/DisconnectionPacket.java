package fr.jamailun.stds.common.standard;

import fr.jamailun.stds.common.JavaPacket;

public class DisconnectionPacket extends JavaPacket {
	private static final long serialVersionUID = 502L;
	public DisconnectionPacket() {
		super("disconnect");
	}
}