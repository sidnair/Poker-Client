package pokerclient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Represents a pot. Contains list of eligible players and information about the
 * pot size.
 */
public class Pot implements Iterable<Player>, Serializable, Cloneable {

	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -5479856379935753328L;

	/**
	 * Size of pot.
	 */
	private int size;

	/**
	 * Set containing all the players in the pot.
	 */
	private HashSet<Player> players;

	/**
	 * Creates a new pot of specified size.
	 * 
	 * @param aSize
	 *            size to initialize pot to
	 */
	public Pot(int aSize) {
		size = aSize;
		players = new HashSet<Player>();
	}

	/**
	 * Alternative constructor for a pot that initializes the size as 0.
	 */
	public Pot() {
		this(0);
	}

	/**
	 * Removes a certain amount of money from the pot.
	 * 
	 * @param aSize
	 */
	public void remove(int aSize) {
		size -= aSize;
	}

	/**
	 * Returns the size of the pot.
	 * 
	 * @return size of pot
	 */
	public int getSize() {
		return size;
	}

	/**
	 * Returns a set of all players eligible for the pot.
	 * 
	 * @return set of all players eligible for the pot
	 */
	public HashSet<Player> getPlayers() {
		return players;
	}

	/**
	 * Returns the number of players in the pot.
	 * 
	 * @return the number of players in the pot
	 */
	public int getNumberEligible() {
		return players.size();
	}

	/**
	 * Adds money to the pot.
	 * 
	 * @param aSize
	 *            amount of money to add to the pot.
	 */
	private void add(int aSize) {
		size += aSize;
	}

	/**
	 * Adds an eligible player to the pot.
	 * 
	 * @param aPlayer player to add
	 */
	public void addPlayer(Player aPlayer) {
		players.add(aPlayer);
	}

	/**
	 * Removes a player from a pot.
	 * 
	 * @param player player to remove
	 */
	public void removePlayer(Player player) {
		players.remove(player);
	}

	/**
	 * Returns players in the pot.
	 * 
	 * @return players in the pot.
	 */
	public Iterator<Player> iterator() {
		return players.iterator();
	}

	/**
	 * String representation of the pot - the size of the pot
	 * 
	 * @return size of pot as a String
	 */
	public String toString() {
		return Integer.toString(size);
	}

	/**
	 * Clones a pot.
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			// shouldn't occur
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Generates a list of pots.
	 * @param players
	 * @return
	 */
	public static ArrayList<Pot> generatePots(HashSet<Player> players) {

		// Have at least a main pot.
		if (players.size() == 0) {
			ArrayList<Pot> pots = new ArrayList<Pot>();
			pots.add(new Pot());
			return pots;
		}

		boolean isAllIn = false;
		for (Player p : players) {
			if (p.isAllIn()) {
				isAllIn = true;
				break;
			}
		}

		ArrayList<Pot> pots;
		if (isAllIn) {
			pots = generatePotsWithSidePots(players);
		} else {
			pots = generatePotsWithoutSidePots(players);
		}

		// Have at least a main pot.
		if (pots.size() == 0) {
			pots.add(new Pot());
		}

		return pots;
	}

	/**
	 * Generate a single pot
	 * @param players
	 * @return list with just the main pot
	 */
	private static ArrayList<Pot> generatePotsWithoutSidePots(
			HashSet<Player> players) {
		ArrayList<Pot> pots = new ArrayList<Pot>();
		Pot pot = new Pot();
		pots.add(pot);
		for (Player p : players) {
			if (p.getTotalPutInPot() == 0) {
				continue;
			}
			pot.add(p.getTotalPutInPot());
			pot.addPlayer(p);
		}

		return pots;
	}

	/**
	 * Generates a list of pots. Side pots are created such that no money is
	 * in a pot of all the players can pay for amt_in_pot / num_players.
	 * @param players
	 * @return
	 */
	private static ArrayList<Pot> generatePotsWithSidePots(
			HashSet<Player> players) {
		ArrayList<Pot> pots = new ArrayList<Pot>();
		ArrayList<Player> orderedPlayers = new ArrayList<Player>(players);
		Collections.sort(orderedPlayers, new Comparator<Player>() {
			@Override
			public int compare(Player first, Player second) {
				return first.getTotalPutInPot() - second.getTotalPutInPot();
			}
		});

		int lastSize = 0;
		for (Player p : orderedPlayers) {
			if (p.getTotalPutInPot() == 0) {
				continue;
			}
			for (Pot pot : pots) {
				pot.add(pot.getSize() / pot.getPlayers().size());
				pot.addPlayer(p);
			}
			int currentSize = p.getTotalPutInPot();
			if (currentSize != lastSize) {
				Pot additionalPot = new Pot();
				additionalPot.add(currentSize - lastSize);
				additionalPot.addPlayer(p);
				pots.add(additionalPot);
			}
			lastSize = currentSize;
		}
		return pots;
	}
}
