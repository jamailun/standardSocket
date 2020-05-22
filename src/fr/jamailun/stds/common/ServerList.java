package fr.jamailun.stds.common;

import fr.jamailun.stds.server.VirtualServer;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class ServerList implements Serializable {
	private static final long serialVersionUID = 1001L;

	transient private Set<VirtualServer> servers = new HashSet<>(); // transient on veut pas que le client le récupère par défault.
	private Set<ServerInfo> serversInfo = new HashSet<>();

	public void updateServersInfo() {
		serversInfo.clear();
		servers.forEach(vs -> serversInfo.add(new ServerInfo(vs.getServerName(), vs.getPort(), vs.getMaxConnected(), vs.getConnected())));
	}

	public Set<ServerInfo> getServers() {
		return new HashSet<>(serversInfo);
	}

	public void registerServer(VirtualServer vs) {
		if(vs == null)
			throw new IllegalArgumentException("VirtualServer cannot be null.");
		for(VirtualServer v : servers) {
			if (v.getId() == vs.getId())
				throw new IllegalArgumentException("A server already exists with this id ("+v.getId()+").");
			if (v.getPort() == vs.getPort())
				throw new IllegalArgumentException("A server already exists with this port ("+v.getPort()+").");
			if (v.getName().equals(vs.getName()))
				throw new IllegalArgumentException("A server already exists with this name ("+v.getName()+").");
		}
		serversInfo.add(new ServerInfo(vs.getServerName(), vs.getPort(), vs.getMaxConnected(), vs.getConnected()));
		servers.add(vs);
	}

	public boolean deregisterServer(VirtualServer virtualServer) {
		boolean notValid = true;
		for(ServerInfo i : serversInfo) {
			if (i.getPort() == virtualServer.getPort()) {
				notValid = false;
				break;
			}
		}
		if(notValid)
			return false;
		serversInfo.removeIf(si -> si.getPort() == virtualServer.getPort());
		servers.removeIf(vss -> vss.getPort() == virtualServer.getPort());
		return true;
	}

	public Iterator<VirtualServer> getVServersIterator() {
		return servers.iterator();
	}
}