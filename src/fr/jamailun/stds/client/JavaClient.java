package fr.jamailun.stds.client;

import fr.jamailun.stds.common.ServerInfo;
import fr.jamailun.stds.common.ServerList;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaClient {

	private ServerList serverList;
	private ProxySocket proxy;
	private ClientListener listener;

	private final String address;
	private final int port;
	public JavaClient(String address, int hubPort) {
		this.address = address;
		this.port = hubPort;
		refreshServerList();
	}

	public void startConnectionToVirtualServer(ServerInfo serverInfo) {
		if(serverInfo != null) {
			try {
				proxy = new ProxySocket(address, serverInfo, listener);
				proxy.start();
			} catch (IOException ee) {
				error("Could not bind to VS " + serverInfo.getName()+":"+serverInfo.getPort()+" : " + ee.getMessage()+".");
			}
		}
	}

	public void refreshServerList() {
		Socket socket;
		try {
			socket = new Socket(address, port);
			log("Connection successful to hub server.");
		} catch (IOException e) {
			error("Impossible to connect : " + e.getMessage());
			return;
		}

		try(ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
			Object object = ois.readObject();
			if(object instanceof ServerList) {
				serverList = (ServerList) object;
				if(serverList.getServers().isEmpty()) {
					error("Server does not have any virtual servers...");
				}
			} else {
				error("Object received is not a ServerList : " + object);
			}
		} catch (IOException | ClassNotFoundException e) {
			error("Impossible to get server list : " + e.getMessage());
		} finally {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isConnectedToVirtualServer() {
		return proxy != null;
	}

	public ProxySocket getProxySocket() {
		return proxy;
	}

	public ServerList getServerList() {
		return serverList;
	}

	private static boolean verbose = false;
	public static void toggleVerbose() {
		verbose =! verbose;
	}
	static void log(String message) {
		if(verbose)
			System.out.println("[CLIENT]["+getDate()+"][INFO] "+message);
	}
	public static boolean verbose() {return verbose;}

	static void error(String message) {
		System.err.println("[CLIENT]["+getDate()+"][ERROR] "+message);
	}

	static void warn(String message) {
		System.out.println("[CLIENT]["+getDate()+"][WARN] "+message);
	}

	private static final SimpleDateFormat sdf = new SimpleDateFormat("ss:mm:hh");
	public static String getDate() {
		return sdf.format(new Date(System.currentTimeMillis()));
	}

	public void setListener(ClientListener listener) {
		this.listener = listener;
	}

	public void removeListener() {listener = null;}

	public boolean haveListener() {
		return listener != null;
	}
}