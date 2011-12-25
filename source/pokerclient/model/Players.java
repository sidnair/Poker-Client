package pokerclient.model;

import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.OperationNotSupportedException;

public class Players implements Iterable<Player> {
	
	private ArrayList<Player> players;

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
	
	public Iterator<Player> activeIterator(final int startIndex) {
		return new Iterator<Player>() {
			
			int i = startIndex;
			
			@Override
			public boolean hasNext() {
				return (i > startIndex) && (i % players.size() == startIndex);
			}

			@Override
			public Player next() {
				while (!players.get(i % players.size()).isInHand()) {
					i++;
				}
				i++;
				return players.get(i % players.size());
			}

			@Override
			public void remove() {
				players.remove(i-1 % players.size());
			}
		};
	}

	@Override
	public Iterator<Player> iterator() {
		return players.iterator();
	}

}
