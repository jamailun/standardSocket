package fr.jamailun.stds.server;

import fr.jamailun.stds.common.ServerList;
import fr.jamailun.stds.server.commands.CommandExecutor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

public class JavaServer extends Thread {

	public boolean running;

	private CommandExecutor commandExecutor;
	private ServerList servers;

	private final int port;
	public JavaServer(int port) {
		this.port = port;
		running = true;
		servers = new ServerList();
		setCommandExecutor(new MultipleCommandsExecutor(this));
	}

	public void setCommandExecutor(CommandExecutor commandExecutor) {
		this.commandExecutor = commandExecutor;
	}
	public CommandExecutor getCommandExecutor() {return commandExecutor;}
	public MultipleCommandsExecutor unsafeGetMultipleCommandExecutor() {
		if(commandExecutor instanceof MultipleCommandsExecutor)
			return (MultipleCommandsExecutor) commandExecutor;
		return null;
	}

	public VirtualServer addNewVirtualServer(String name, int port, int maxConnected) {
		VirtualServer vs = new VirtualServer(this, name, port, maxConnected);
		servers.registerServer(vs);
		vs.start();
		return vs;
	}

	public boolean removeVirtualServer(int port) {
		VirtualServer vs = getWithPort(port);
		if(vs == null)
			return false;
		vs.shutdown();
		return servers.deregisterServer(vs);
	}

	public VirtualServer getWithName(String name) {
		for (Iterator<VirtualServer> it = servers.getVServersIterator(); it.hasNext(); ) {
			VirtualServer vs = it.next();
			if(vs.getName().equals(name))
				return vs;
		}
		return null;
	}

	public VirtualServer getWithPort(int port) {
		for (Iterator<VirtualServer> it = servers.getVServersIterator(); it.hasNext(); ) {
			VirtualServer vs = it.next();
			if(vs.getPort() == port)
				return vs;
		}
		return null;
	}

	/**
	 * Get the editable server list.
	 * @return the current {@link ServerList ServerList}.
	 */
	public ServerList getServerList() {
		servers.updateServersInfo();
		return servers;
	}


	private static boolean verbose = false;
	public static void toggleVerbose() {
		verbose =! verbose;
	}
	public static boolean verbose() {
		return verbose;
	}

	static void log(String message) {
		if(verbose)
			System.out.println("[SERVER]["+getDate()+"][INFO] "+message);
	}

	static void error(String message) {
		System.err.println("[SERVER]["+getDate()+"][ERROR] "+message);
	}

	private static final SimpleDateFormat sdf = new SimpleDateFormat("ss:mm:hh");
	public static String getDate() {
		return sdf.format(new Date(System.currentTimeMillis()));
	}

	public void stopRunning() {
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	public void start() {
		if(commandExecutor != null)
			commandExecutor.startThread();
		try {
			ServerSocket server = new ServerSocket(port);
			log("Starting listening on hub port :" +port+".");
			while(running) {
				try {
					Socket socket = server.accept();
					//log("New connection from " + socket+". - " +servers.getServers().size());
					new PingServerList(socket, this).start();

				} catch (IOException e) {
					error("Impossible to connect to socket. " + e.getMessage());
				}
			}
		} catch (IOException e) {
			error("Failed to listening on port "+port+" : " + e.getMessage());
		}
	}

	public void shutdown() {
		stopRunning();
		servers.getVServersIterator().forEachRemaining(VirtualServer::shutdown);
		log("Termination successful.");
		System.exit(0);
	}
}