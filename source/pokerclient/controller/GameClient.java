package pokerclient.controller;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import pokerclient.gui.GameView;

public class GameClient implements PropertyChangeListener, Runnable {
	
	private static final String NULL_PATH = "NULL";
	private static final String DEFAULT_PATH = "images/avatars/panda.png";
	
	private static final String PASS = "supersecret";
	
	// TODO - the server should be setting this
	private static final int TIME_BANK = 50000;
	private static final String USAGE =
			"Usage: java GameClient host port playerName pathToAvatar";	
	private Socket socket;
	private GameView view;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public GameClient(String host, int port, String name, String path) {
		// TODO - make sure that the view gets updated if the server says that
		// the BB or time bank is different.
		view = new GameView(this, name, TIME_BANK, GameServer.DEFAULT_BB,
				host.contains(PASS));
		if (host.contains(PASS)) {
			host = host.replace(PASS, "");
		}
		view.run();
		try {
			socket = new Socket(host, port);
			initializeOutStream(name, path);
			new Thread(this).start();
			System.out.println(host + "\t" + port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 *  Establish connection and write name and avatar path. Will terminate on
	 *  error.
	 */
	private void initializeOutStream(String name, String path) {
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(name);
			out.flush();
			if (path.equals(NULL_PATH)) {
				path = DEFAULT_PATH;
			}
			out.writeObject(path);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	public void run() {
		// TODO - can this be at the end of the constructor?
        try {
            in = new ObjectInputStream((socket.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
		while (true) {
			try {
				PropertyChangeEvent pce = (PropertyChangeEvent) in.readObject();
//				view.propertyChange(pce);
			} catch (IOException e) {
				// TODO - terminate?
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO - terminate?
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 3) {
			new GameClient(args[0], Integer.parseInt(args[1]), args[2], NULL_PATH);
		} else if (args.length == 4) {
			new GameClient(args[0], Integer.parseInt(args[1]), args[2], args[3]);
		} else {
			System.err.println(USAGE);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(GameView.PLAYER_ACTION)) {
			try {
				out.writeObject(evt.getNewValue());
                out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		
}