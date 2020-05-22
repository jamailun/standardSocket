package fr.jamailun.stds.common;

import java.io.Serializable;

public final class ServerInfo implements Serializable {
	private static final long serialVersionUID = 1002L;

	private final String name;
	private final int port, maxConnected, connected;
	public ServerInfo(String name, int port, int maxConnected, int connected) {
		this.name = name;
		this.port = port;
		this.maxConnected = maxConnected;
		this.connected = connected;
	}

	public String getName() {
		return name;
	}

	public int getPort() {
		return port;
	}

	public int getMaxConnected() {
		return maxConnected;
	}

	public int getConnected() {
		return connected;
	}

	public String toString() {
		return "VS["+getName()+"]:"+getPort()+" - [" + getConnected()+"/"+getMaxConnected()+"]";
	}
}
