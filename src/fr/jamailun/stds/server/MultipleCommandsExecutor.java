package fr.jamailun.stds.server;

import fr.jamailun.stds.common.ClientInfo;
import fr.jamailun.stds.common.ServerInfo;
import fr.jamailun.stds.common.standard.ServerBroadcastPacket;
import fr.jamailun.stds.server.commands.Command;
import fr.jamailun.stds.server.commands.CommandExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class MultipleCommandsExecutor implements CommandExecutor {

	private final List<Command> commands;

	public MultipleCommandsExecutor(JavaServer server) {
		commands = new ArrayList<>();
		commands.add(new Command("help", args -> {
			log("---- > AIDE <----");
			commands.forEach(c -> log("-"+c.getLabel() + " : " + c.getDescription() + ". Aliases : " + Arrays.toString(c.getAliases())));
		}, "Display this help menu","?", "aide"));

		commands.add(new Command("stop", args -> {
			log("Shutdown...");
			valid = false;
			server.shutdown();
		}, "Stop all virtual servers and this main one.","end", "exit", "quit"));

		commands.add(new Command("broadcast", args -> {
			log("Send broadcast message threw all virtual servers.");
			if(args.length == 0) {
				error("Please enter a message.");
				return;
			}
			StringBuilder b = new StringBuilder();
			for (String arg : args) b.append(arg).append(" ");
			server.getServerList().getVServersIterator().forEachRemaining(vs -> vs.sendPacketToClients(new ServerBroadcastPacket(b.toString())));
		}, "Broadcast message threw all virtual servers. Usage : /broadcast <message>.","say", "alert", "!", "bc"));

		commands.add(new Command("verbose", args -> {
			JavaServer.toggleVerbose();
			log("Toggled verbose. [Verbose :" + (JavaServer.verbose() ? "ON" : "OFF")+"].");
		}, "Toggle the verbose of the server."));

		commands.add(new Command("serversList", args -> {
			log("VS list ("+server.getServerList().getServers().size()+"):");
			for(ServerInfo si : server.getServerList().getServers())
				log(" " + si);
		}, "Get the list of all virtual servers.", "sl", "servers"));

		commands.add(new Command("clients", args -> {
			if(args.length == 0) {
				error("Precise the port of the virtual server.");
				return;
			}
			VirtualServer vs = null;
			try {
				vs = server.getWithPort(Integer.parseInt(args[0]));
			} catch (NumberFormatException ignored) {}
			if(vs == null) {
				error("Unknown server port : " + args[0]+".");
				return;
			}
			log("Clients connected ("+vs.getClientsInfo().size()+"):");
			for(ClientInfo ci : vs.getClientsInfo())
				log("-> " + ci.toString());
		}, "Get all clients of specific server. Usage : /clients <portID>", "cl"));

		commands.add(new Command("createVS", args -> {
			if(args.length < 2) {
				error("Usage : /createVS <port> <name>.");
				return;
			}
			int port;
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException ignored) {
				error("Bad number format. Usage : /createVS <port> <name>.");
				return;
			}
			if(port < 1000) {
				error("Port < 1000 is unsafe.");
				return;
			}
			if(port > 90000) {
				error("Port > 90000 is illegal.");
				return;
			}
			StringBuilder b = new StringBuilder();
			for (int i = 1; i < args.length; i++) b.append(args[i]).append(" ");
			if ( server.addNewVirtualServer(b.toString(), port,100) == null )
				error("Unknown error. Maybe port is taken ? Maybe name is not correct ?");
			else
				log("The new virtual server has been created on port " + port + ".");
		}, "Create a new Virtual Server. Usage : /createSV <port> <name>", "cvs"));

		commands.add(new Command("removeVS", args -> {
			if(args.length < 1) {
				error("Usage : /removeVS <port>.");
				return;
			}
			int port;
			try {
				port = Integer.parseInt(args[0]);
			} catch (NumberFormatException ignored) {
				error("Bad number format. Usage : /createVS <port>.");
				return;
			}
			if( ! server.removeVirtualServer(port) )
				error("No virtual server have been found with port " + port+".");
			else
				log("Success : virtual server on port " + port + " has been deleted.");

		}, "Remove a new Virtual Server. Usage : /createSV <portID>", "rvs", "dvs"));

	}

	@Override
	public void executeCommand(String label, String[] args) {
		for(Command command : commands) {
			if(command.matches(label)) {
				command.getTreatment().execute(args);
				return;
			}
		}
		error("Unknown command : " + label+". Type 'help' to get the list of all commands.");
	}

	public boolean registerCommand(Command command) {
		for(Command cmd : commands) {
			if(cmd.matches(command.getLabel()) || command.matches(cmd.getLabel()))
				return false;
			for(String a : command.getAliases())
				if(cmd.matches(a))
					return false;
			for(String a : cmd.getAliases())
				if(command.matches(a))
					return false;
		}
		commands.add(command);
		return true;
	}

	public boolean registerCommand(String label, Command.CommandTreatment treatment, String description, String... aliases) {
		return registerCommand(new Command(label, treatment, description, aliases));
	}

	private boolean valid = true;
	@Override
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
			error("Bad command format !");
		}
	}
	private void log(String message) {
		System.out.println("[SERVER]["+JavaServer.getDate()+"][>] "+message);
	}
	private void error(String message) {
		System.err.println("[SERVER]["+JavaServer.getDate()+"][ERROR] "+message);
	}

}