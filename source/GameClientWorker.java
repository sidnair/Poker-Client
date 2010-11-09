import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class GameClientWorker implements Runnable {
	
	private static String newLine = System.getProperty("line.separator");
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Player player;
	private PropertyChangeListener listener;
	private boolean playing;
	  
	  GameClientWorker(ObjectInputStream in, ObjectOutputStream out, 
			  Player player, PropertyChangeListener listener) {
		this.in = in;
        this.out = out;
		this.player = player;
        this.listener = listener;
        playing = true;
	  }

          GameClientWorker(Socket socket, Player player) {
		this.player = player;
		try {
			out = new ObjectOutputStream(socket.getOutputStream());
			in = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  }
	  
	  public void run(){
		    while(playing){
		      try{
		    	  Action action = null;
				try {
                    action = (Action) in.readObject();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (player.getName().equals(action.getPlayerName())) {
		    		  player.acceptAction(action);
				}
		       } catch (IOException e) {
		    	   listener.propertyChange(new PropertyChangeEvent(this, 
		    			   GameServer.PLAYER_QUIT, new Object(), player));
		    	   player.fold();
		    	   System.out.println(player.getName() + " quit");
		    	   playing = false;
		       }
		    }
	  }
	  
	  public void setPlaying(boolean b) {
		  playing = b;
	  }
	  
	  public Player getPlayer() {
		  return player;
	  }
	  
	  public void sendChange(PropertyChangeEvent evt) {
		  if (playing) {
			 try {
	            out.writeObject(evt);
	            out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.out.println("IO Error: connection closed");
			}
		  }
	  }

}
