package fr.jamailun.stds.common.standard;

import fr.jamailun.stds.common.JavaPacket;

public class RenamePacket extends JavaPacket {
	private final String name;
	public RenamePacket(String name) {
		super("rename");
		this.name = name;
	}
	public String getName() {
		return name;
	}
}