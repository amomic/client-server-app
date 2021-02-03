package at.tugraz.oop2.server;

import org.apache.commons.cli.*;

/**
 * Used to start the server. The actual implementation should be in other classes.
 */
public final class ServerMain {

	public static void main(String... args) {
		CommandLine cmd = validateArgs(args);
		System.out.println(cmd);
		if (cmd != null) {
			final int serverPort = Integer.parseUnsignedInt(cmd.getOptionValue("port"));
			String data_path = cmd.getOptionValue("data", "data/sensors");
			final AnalysisServer server = new AnalysisServer(serverPort, data_path);
			server.run();
			// control flow never reaches here
		}
	}

	private static CommandLine validateArgs(String... args){
		Options options = new Options();
		Option dataOption = new Option("d", "data", true, "data path");
		dataOption.setRequired(false);
		options.addOption(dataOption);
		Option portOption = new Option("p", "port", true, "server port");
		portOption.setRequired(true);
		options.addOption(portOption);
		try {
			return new DefaultParser().parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			System.out.println(e.getMessage());
			formatter.printHelp("server parameters", options);
			return null;
			}
	}
}
