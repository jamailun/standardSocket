import fr.jamailun.stds.common.JavaPacket;
import fr.jamailun.stds.server.*;

public class TestsServer {

	public static void main(String[] a) {
		JavaServer server = new JavaServer(8181);
		VirtualServer vs = server.addNewVirtualServer("TESTS", 8181+1, 20);

		vs.registerListener(new ServerListener() {
			@Override
			public void packetReceived(JavaPacket packet, ConnectedSocket client) {
				System.out.println("RECU"+packet);
			}

			@Override
			public void clientDisconnected(ConnectedSocket client) {
				System.out.println("[-] Client disconnected :" + client + "   ("+ vs.getConnected() + "/"+vs.getMaxConnected()+")");
			}

			@Override
			public void clientConnected(ConnectedSocket client) {
				System.out.println("[+] Client connected :" + client + "   ("+ vs.getConnected() + "/"+vs.getMaxConnected()+")");
			}

			@Override
			public void virtualRoomShutdowns() {
				System.out.println("[X] Fin du game.");
			}
		});
		server.start();

	}

}
