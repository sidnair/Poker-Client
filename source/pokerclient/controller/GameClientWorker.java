package pokerclient.controller;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import pokerclient.model.Action;
import pokerclient.model.Player;

// TODO - refactor; unclear if the playing variable is needed since it doesn't
// seem possible for a player to reconnect.
public class GameClientWorker implements Runnable {
	
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
			   e.printStackTrace();
			   listener.propertyChange(new PropertyChangeEvent(this, 
					   GameServer.PLAYER_QUIT, null, player));
			   player.fold();
	    	   System.out.println(player.getName() + " quit.");
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
				System.out.println("IO Error: connection closed");
			}
		  }
	  }

}
