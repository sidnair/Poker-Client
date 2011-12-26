package pokerclient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class Players implements Iterable<Player>, Serializable {
	
	/**
	 * Automatically generated serial ID. 
	 */
	private static final long serialVersionUID = 8437955403272755772L;
	
	private ArrayList<Player> players;
	
	private int buttonIndex;

	public Players() {
		players = new ArrayList<Player>();
	}
	
	/**
	 * Adds a player.
	 * 
	 * TODO - right now it adds at the end of the array, but we really want
	 * to add it after the bb.
	 * 
	 * @param p Player to add
	 * @precondition don't add while iterating through the players
	 */
	public void addPlayer(Player p) {
		players.add(p);
	}

	/*
	 * Keeps a list of players and allows iteration in a way
	 * that wraps around the indeces.
	 */
	
	public Iterator<Player> inHandIterator(final int startIndex) {
		return new Iterator<Player>() {
			
			int i = startIndex;
			
			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public Player next() {
				while (!players.get(i % players.size()).isInHand()) {
					i++;
				}
				i++;
				return players.get((i - 1) % players.size());
			}

			@Override
			public void remove() {
				players.remove((i - 1) % players.size());
			}
		};
	}

	@Override
	public Iterator<Player> iterator() {
		return players.iterator();
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Player> getPlayersCopy() {
		return (ArrayList<Player>) players.clone();
	}
	
	public int size() {
		return players.size();
	}
	
	public boolean contains(Player p) {
		return players.contains(p);
	}
	
	public void remove(Player p) {
		players.remove(p);
	}
	
	public Player get(int i) {
		return players.get(i % players.size());
	}

	public int inHandCount() {
		int inHand = 0;
		for (int i = 0; i < players.size(); i++) {
			if (players.get(i).isInHand()) {
				inHand++;
			}
		}
		return inHand;
	}

	public ArrayList<Player> inHand(int start) {
		ArrayList<Player> inHandPlayers = new ArrayList<Player>();
		for (int i = start; i < players.size() + start; i++) {
			Player p = players.get(i % players.size());
			if (p.isInHand()) {
				inHandPlayers.add(p);
			}
		}
		return inHandPlayers;
	}
	
	/*
	 * Returns a list of players in the order in which they would have to act.
	 */
	public ArrayList<Player> inHand() {
		return inHand(buttonIndex + 1);
	}

	public int indexOf(Player active) {
		return players.indexOf(active);
	}
	
	public void moveButton() {
		buttonIndex++;
	}
	
	public int getButtonIndex() {
		return buttonIndex;
	}
	
	public Player getSB() {
		return get(getSBIndex());
	}
	
	public Player getBB() {
		return get(getBBIndex());
	}
	
	public int getSBIndex() {
		return buttonIndex + getSBOffset();
	}
	
	public int getBBIndex() {
		return buttonIndex + getBBOffset();
	}
	
	public int getSBOffset() {
		boolean hu = (this.inHandCount() == 2);
		return hu ? 0 : 1;
	}
	
	public int getBBOffset() {
		boolean hu = (this.inHandCount() == 2);
		return hu ? 1 : 2;
	}

}
