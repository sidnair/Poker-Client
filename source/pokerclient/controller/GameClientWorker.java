package pokerclient.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import pokerclient.model.Action;
import pokerclient.model.GameModel;
import pokerclient.model.Player;

// TODO - refactor; unclear if the playing variable is needed since it doesn't
// seem possible for a player to reconnect.
public class GameClientWorker implements Runnable {
	
	private ObjectInputStream in;
	private ObjectOutputStream out;
	private Player player;
	private PropertyChangeListener listener;
	private boolean playing;
	  
	  GameClientWorker(Socket socket, GameModel model,
			  ReentrantLock workersLock, ArrayList<GameClientWorker> workers,
			  PropertyChangeListener listener) throws IOException, ClassNotFoundException {
		  System.out.println("GCW start");
		  this.out = new ObjectOutputStream(socket.getOutputStream());
		  this.in = new ObjectInputStream(socket.getInputStream());
		  JoinSettings settings = (JoinSettings) in.readObject();
	      this.player = new Player(settings.getName(), settings.getAvatarPath(),
	    		  model.getSettings(), model);
	      System.out.println("New player: " + settings.getName() + " \t" + settings.getAvatarPath());

	      this.listener = listener;
		  playing = true;

          model.addPlayer(player);
          workersLock.lock();
	      workers.add(this);
	      workersLock.unlock();
	  }
	  
	  public void run(){
		 while (playing) {
		   try {
		    	Action action = null;
		    	action = (Action) in.readObject();
		    	if (player.getName().equals(action.getPlayerName())) {
		    		  player.acceptAction(action);
				}
		   } catch (ClassNotFoundException e) {
			   e.printStackTrace();
		   } catch (IOException e) {
			   System.out.println("Player " + player.getName() + " exited.");
			   player.fold();
	    	   playing = false;
			   listener.propertyChange(new PropertyChangeEvent(this, 
					   GameServer.PLAYER_QUIT, null, player));
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
				System.out.println("IO Error: connection closed");
			}
		  }
	  }

}
