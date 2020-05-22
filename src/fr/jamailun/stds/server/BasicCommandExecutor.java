package fr.jamailun.stds.server;

import fr.jamailun.stds.common.ClientInfo;
import fr.jamailun.stds.common.ServerInfo;
import fr.jamailun.stds.common.standard.ServerBroadcastPacket;
import fr.jamailun.stds.server.commands.CommandExecutor;

import java.util.Scanner;

public class BasicCommandExecutor implements CommandExecutor {

	private final JavaServer server;
	BasicCommandExecutor(JavaServer server) {
		this.server = server;
	}

	private void log(String message) {
		System.out.println("[SERVER]["+JavaServer.getDate()+"][>] "+message);
	}
	private void error(String message) {
		System.err.println("[SERVER]["+JavaServer.getDate()+"][ERROR][>] "+message);
	}
	@Override
	public void executeCommand(String label, String[] args) {
		if(label.equalsIgnoreCase("stop") || label.equalsIgnoreCase("exit") || label.equalsIgnoreCase("end")) {
			log("Shutdown...");
			valid = false;
			server.shutdown();
			return;
		}
		if(label.equalsIgnoreCase("help") || label.equalsIgnoreCase("?")) {
			log("Commands : help, stop, sl, clients <vs>, say, verbose.");
			return;
		}
		if(label.equalsIgnoreCase("servers") || label.equalsIgnoreCase("serversList") || label.equalsIgnoreCase("sl")) {
			log("VS list ("+server.getServerList().getServers().size()+"):");
			for(ServerInfo si : server.getServerList().getServers())
				log("-> " + si);
			return;
		}
		if(label.equalsIgnoreCase("say") || label.equalsIgnoreCase("broadcast") || label.equalsIgnoreCase("alert")) {
			log("Send broadcast message threw all virtual servers.");
			if(args.length == 0) {
				error("Please enter a message.");
				return;
			}
			StringBuilder b = new StringBuilder();
			for (String arg : args) b.append(arg).append(" ");
			server.getServerList().getVServersIterator().forEachRemaining(vs -> vs.sendPacketToClients(new ServerBroadcastPacket(b.toString())));
			return;
		}
		if(label.equalsIgnoreCase("verbose")) {
			JavaServer.toggleVerbose();
			log("Toggled verbose. [Verbose :" + (JavaServer.verbose() ? "ON" : "OFF")+"].");
			return;
		}
		if(label.equalsIgnoreCase("clients") || label.equalsIgnoreCase("list") || label.equalsIgnoreCase("clientsList")) {
			if(args.length == 0) {
				error("Precise the name/port of the virtual server.");
				return;
			}
			VirtualServer vs = server.getWithName(args[0]);
			if(vs == null) {
				try {
					vs = server.getWithPort(Integer.parseInt(args[0]));
				} catch (NumberFormatException ignored) {}
				if(vs == null) {
					error("Unknown server name/port : " + args[0]+".");
					return;
				}
			}
			log("Clients connected ("+vs.getClientsInfo().size()+"):");
			for(ClientInfo ci : vs.getClientsInfo())
				log("-> " + ci.toString());
			return;
		}
		error("Unknown command.");
	}

	private boolean valid = true;
	public void startThread() {
		new Thread(() -> {
			while(valid) {
				Scanner sc = new Scanner(System.in);
				String cmd;
				do {
					cmd = sc.nextLine();
				} while (cmd == null || cmd.isEmpty());
				command(cmd);
			}
		}).start();
	}

	private void command(String cmd) {
		String[] words = cmd.split(" ");
		try {
			String label = words[0];
			String[] args = new String[words.length - 1];
			if (label.length() - 1 >= 0)
				System.arraycopy(words, 1, args, 0, words.length - 1);
			executeCommand(label, args);
		} catch (IndexOutOfBoundsException e) {
			error("Bad command !");
		}

	}
}
