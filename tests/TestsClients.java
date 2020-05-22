import fr.jamailun.stds.client.ClientListener;
import fr.jamailun.stds.client.JavaClient;
import fr.jamailun.stds.common.JavaPacket;
import fr.jamailun.stds.common.standard.RenamePacket;
import fr.jamailun.stds.common.standard.ServerResponsePacket;

import java.util.Scanner;

public class TestsClients {
	public static void main(String[] args) {
		JavaClient client = new JavaClient("127.0.0.1", 8181);
		client.setListener(new ClientListener() {
			@Override
			public void packetReceived(JavaPacket packet) {
				System.out.println("Reçu paquet : "+ packet);
				if(packet instanceof ServerResponsePacket) {
					System.out.println("Réponse du serveur : "+ ((ServerResponsePacket)packet).getAnswer());
				}
			}

			@Override
			public void connectionInitializedWithVS() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.out.print("Enter new name : ");
				String pseudo = requestString();
				System.out.println("Renaming into -> " + pseudo);
				if(client.isConnectedToVirtualServer())
						client.getProxySocket().sendPacket(new RenamePacket(pseudo));
				else
					System.err.println("La connection est fermée !");
			}

			@Override
			public void serverDisconnected() {
				System.err.println("Server disconnected us.");
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				System.exit(0);
			}

			@Override
			public void serverBroadcastMessage(String message) {
				System.out.println("[BROADCAST] -> "+message);
			}
		});

		client.startConnectionToVirtualServer(client.getServerList().getServers().iterator().next());
	}
	public static String requestString() {
		Scanner sc = new Scanner(System.in);
		String str;
		do {
			str = sc.nextLine();
		} while (str == null || str.isEmpty());
		return str;
	}
}