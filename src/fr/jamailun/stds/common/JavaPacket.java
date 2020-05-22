package fr.jamailun.stds.common;

import java.io.Serializable;

public abstract class JavaPacket implements Serializable {
	private static final long serialVersionUID = 500L;

	private final String type;
	private final long timeStamp = System.currentTimeMillis();

	public JavaPacket(String type) {
		this.type = type;
	}
	public final String getType() {
		return type;
	}
	public String toString() {
		return "JavaPacket@[type="+type+"]";
	}
	public long getTimeStamp() {
		return timeStamp;
	}
}