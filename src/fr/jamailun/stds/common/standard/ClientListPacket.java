package fr.jamailun.stds.common.standard;

import fr.jamailun.stds.common.ClientInfo;
import fr.jamailun.stds.common.JavaPacket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ClientListPacket extends JavaPacket {
	private final List<ClientInfo> clients;
	public ClientListPacket(Collection<ClientInfo> clients) {
		super("clientList");
		this.clients = new ArrayList<>(clients);
	}
	public List<ClientInfo> getClients() {
		return clients;
	}
}