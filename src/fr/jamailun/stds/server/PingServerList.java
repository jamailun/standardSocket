package fr.jamailun.stds.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

class PingServerList extends Thread {

	private final Socket client;
	private final JavaServer server;
	PingServerList(Socket client, JavaServer server) {
		this.client = client;
		this.server = server;
	}

	@Override
	public void run() {
		try(ObjectOutputStream oos = new ObjectOutputStream(client.getOutputStream())) {
			oos.writeObject(server.getServerList());
			oos.flush();
			client.close();
		} catch (IOException e) {
			JavaServer.error("Impossible to send serverList to client ("+client+") : " + e.getMessage());
			e.printStackTrace();
		}
	}
}
