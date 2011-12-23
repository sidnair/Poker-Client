package pokerclient.drivers;

import pokerclient.controller.GameServer;

public class ServerDriver {
	
	private static final String USAGE = "java GameServer port [frameName]";

	public static void main(String[] args) {
		if (args.length == 1) {
			new GameServer(Integer.parseInt(args[0]));
		} else if (args.length == 2) {
			new GameServer(Integer.parseInt(args[0]), args[1]);
		} else {
			System.err.println(USAGE);
		}
	}
}
