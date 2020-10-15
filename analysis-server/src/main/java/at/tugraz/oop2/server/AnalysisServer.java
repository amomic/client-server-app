package at.tugraz.oop2.server;

/**
 * This class will hold the implementation of your server and handle connecting and connected clients.
 */
public final class AnalysisServer {
		private final int serverPort;
		private final String dataPath;

		public AnalysisServer(int serverPort, String dataPath) {
				this.serverPort = serverPort;
				this.dataPath = dataPath;
		}

		public void run() {
				// TODO Start here with a loop accepting new client connections.
		}
}


