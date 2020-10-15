package at.tugraz.oop2.client;

import java.io.IOException;
import java.util.Arrays;

public final class ClientMain {

	public static void main(String... args) {
		if (args.length >= 2) {
			final String url = args[0];
			final int port = Integer.parseUnsignedInt(args[1]);
			try {
				ClientConnection conn = new ClientConnection();

				conn.addConnectionClosedListener(() -> System.out.println("Client disconnected."));
				conn.connect(url, port);
				final CommandHandler handler = new CommandHandler(conn);
				if (args.length == 2) {
					handler.openCLI();
				} else {
					handler.handle(Arrays.copyOfRange(args, 2, args.length));
				}
			} catch (final IOException ex) {
					System.err.println("Couldn't connect to server: " + url + " and port: " + port);
			}
		} else {
			printUsage();
		}
	}

	private static void printUsage() {
		System.out.println("Usage:");
		System.out.println("  ./client.jar <server url> <server port> - Connects to the server and opens the CLI");
		System.out.println("  ./client.jar <server url> <server port> <cmd> <params ...> - Connects to the server and executes one command");
	}
}
