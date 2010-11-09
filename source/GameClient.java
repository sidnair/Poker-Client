import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class GameClient implements PropertyChangeListener, Runnable {
	
	private Socket socket;
	private GameView view;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private static final int TIME_BANK = 500000;
	private static final String NULL_PATH = "NULL";
	private static final int BIG_BLIND = 10;
	
	public GameClient(String tempHost, int tempPort, String name, String path) {
		String pw = "topsecret";
		view = new GameView(this, name, TIME_BANK, BIG_BLIND, tempHost.contains(pw));
		if (tempHost.contains(pw)) {
			tempHost = tempHost.replace(pw, "");
		}
		view.run();
		try {
			socket = new Socket(tempHost, tempPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			out.writeObject(name);
			out.flush();
			if (path.equals(NULL_PATH)) {
				path = "images/avatars/panda.png";
			}
			out.writeObject(path);
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //TODO: fix)
		new Thread(this).start();
	}
	
	public void run() {
            try {
                in = new ObjectInputStream((socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
		while(true) {
			try {
				try {
					view.propertyChange((PropertyChangeEvent) in.readObject());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		if (args.length >= 3) {
			new GameClient(args[0], Integer.parseInt(args[1]), args[2], args[3]);
		} else {
			new GameClient(args[0], Integer.parseInt(args[1]), args[2], NULL_PATH);
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