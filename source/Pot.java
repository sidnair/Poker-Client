import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Represents a pot. Contains list of eligible players and information about the
 * pot size.
 * 
 * @author Sid Nair
 *
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
	 * Active players in pot. Conceptually, the players in a pot would be a set.
	 * However, calculating side pots requires a concept of order. Thus, this
	 * variable is stored and updated as necessary to avoid having to create
	 * it each time a side pot is made.
	 */
	private ArrayList<Player> myPlayers;
	
	/**
	 * Size of last raise made by the player.
	 */
	private int lastRaiseSize;
	
	/**
	 * ID number associated with the pot.
	 */
	private int id;
	
	/**
	 * Number used to create a pot's ID and ensure each pot's ID is unique.
	 */
	private static int potIndex = 0;
	
	/**
	 * Smallest size which has been accepted into the pot.
	 */
	private int smallestAcceptedSize;
	
	/**
	 * Set containing all the players in the pot.
	 */
	private HashSet<Player> playerSet;
	
	/**
	 * Creates a new pot of specified size.
	 * 
	 * @param aSize size to initialize pot to
	 */
	public Pot(int aSize) {
		size = aSize;
		myPlayers = new ArrayList<Player>();
		playerSet = new HashSet<Player>();
		potIndex++;
		this.id = potIndex;
	}
	
	/**
	 * Resets the counter for making IDs.
	 */
	public static void resetCounter() {
		potIndex = 0;
	}
	
	/**
	 * Alternative constructor for a pot that initializes the size as 0. This
	 * is written for user convenience.
	 */
	public Pot() {
		this(0);
	}
	
	/**
	 * Removes a certain amount of money from the pot.
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
	 * Resets the last raise size the pot has accepted.
	 */
	public void resetLastRaiseSize() {
		lastRaiseSize = 0;
	}
	
	/**
	 * Returns the last raise size the pot has accepted.
	 * 
	 * @return the last raise size the pot has accepted.
	 */
	public int getLastRaiseSize() {
		return lastRaiseSize;
	}
	
	/**
	 * Returns the ID number associated with the pot.
	 * 
	 * @return the ID number associated with the pot
	 */
	public int getID() {
		return id;
	}	
	
	/**
	 * Returns a list of all the additional side pots resulting from the 
	 * attempted raise.
	 * 
	 * @param attemptedSize size of the attempted raise
	 * @return list of all the new side pots created
	 */
	public ArrayList<Pot> calcSidePots(int attemptedSize) {
		ArrayList<Pot> potList = new ArrayList<Pot>();
		Pot tempPot = new Pot();
		Collections.sort(myPlayers, makeLastSizeComparator());
		for (Player player : myPlayers) {
			if (player.getLastSize() > attemptedSize) {
				//TODO: you get added to this side pot even if you can't pay for it?
				if (player.getLastSize() - attemptedSize < tempPot.getLastRaiseSize()) { 
					tempPot.add(player.getLastSize() - attemptedSize, player, true);
					this.size -= player.getLastSize() - attemptedSize;
					player.setLastSize(attemptedSize);
				} else {
					tempPot.add(player.getLastSize() - attemptedSize, player, false);
					this.size -= player.getLastSize() - attemptedSize;
					player.setLastSize(attemptedSize);
					ArrayList<Pot> tempPotList = tempPot.calcSidePots(player.getLastSize() - attemptedSize);
					potList.addAll(tempPotList);
				}
			} else if (player.getLastSize() > smallestAcceptedSize) {
				
			}
		}
		potList.add(tempPot);
		return potList;
	}
	
	/**
	 * Returns a set of all players eligible for the pot. 
	 * 
	 * @return set of all players eligible for the pot
	 */
	public HashSet<Player> getPlayerSet() {
		return playerSet;
	}
	
	/**
	 * Returns the number of players in the pot.
	 * 
	 * @return the number of players in the pot
	 */
	public int getNumberEligible() {
		return playerSet.size();
	}
	
	/**
	 * Merges two pots. It is assumed that the players in these two pots are
	 * the same.
	 * 
	 * @param other pot from which to get a sizing.
	 */
	public void merge(Pot other) {
		other.myPlayers.clear();
		other.playerSet.clear();
		this.add(other.size);
		other.size = 0;
	}
	
	/**
	 * Adds money to the pot.
	 * 
	 * @param aSize amount of money to add to the pot.
	 */
	private void add(int aSize) {
		size += aSize;
	}
	
	/**
	 * Adds money to pot.
	 * 
	 * @param aSize money to add to the pot.
	 * @param aPlayer player who is adding money to the pot
	 * @param canPay true if the player has enough money to call the existing
	 * raise
	 */
	public void add(int aSize, Player aPlayer, boolean canPay) {
		size += aSize;
		addPlayer(aPlayer);
		if (canPay) {
			lastRaiseSize = aSize;
		} else {
			if (aSize < smallestAcceptedSize || lastRaiseSize == 0) {
				smallestAcceptedSize = aSize;
			}
		}
	}
	
	/**
	 * Adds and eligible player to the pot.
	 * 
	 * @param aPlayer player to add
	 * @precondition player not already in pot
	 */
	public void addPlayer(Player aPlayer) {
		if (!myPlayers.contains(aPlayer)) {
			myPlayers.add(aPlayer);
			playerSet.add(aPlayer);
		}
	}
	
	/**
	 * Removes a player from a pot.
	 * 
	 * @param aPlayer player to remove
	 * @return true if the list of players has been changed
	 */
	public boolean removePlayer(Player aPlayer) {
		playerSet.remove(aPlayer);
		return myPlayers.remove(aPlayer);
	}
	
	/**
	 * Returns players in the pot.
	 * 
	 * @return players in the pot.
	 */
	public Iterator<Player> iterator() {
		return playerSet.iterator();
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
	 * Makes a comparator that compares the last size of the raises each 
	 * player has made.
	 * 
	 * @return a comparator that compares the last size of the 
	 * raises each player has made. 
	 */
	public Comparator<Player> makeLastSizeComparator() {
		return new Comparator<Player>() {
			@Override
			public int compare(Player one, Player two) {
				if (one.getLastSize() > two.getLastSize()) {
					return -1;
				} else if (one.getLastSize() < two.getLastSize()) {
					return 1;
				}
				return 0;
			}
		};
	}

	/**
	 * Clones a pot.
	 */
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			//shouldn't occur
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<Pot> generatePots(HashSet<Player> players) {
		ArrayList<Pot> pots = new ArrayList<Pot>();
		
		if (players.size() == 0) {
			return pots;
		}
		
		ArrayList<Player> orderedPlayers = new ArrayList<Player>(players);
		Collections.sort(orderedPlayers, new Comparator<Player>() {
			@Override
			public int compare(Player first, Player second) {
				return first.getLastSize() - second.getLastSize();
			}
		});
		
		// TODO - check that getLastSize returns the total
		int lastSize = 0;
		for (Player p : orderedPlayers) {
			System.out.println("here");
			for (Pot pot : pots) {
				System.out.println("add " + pot.getSize() / pot.getPlayerSet().size());
				System.out.println(pot.getPlayerSet().size());
				pot.add(pot.getSize() / pot.getPlayerSet().size());
				pot.addPlayer(p);
			}
			int currentSize = p.getLastSize();
			if (currentSize != lastSize) {	
				Pot additionalPot = new Pot();
				System.out.println("add down " + (currentSize - lastSize));
				additionalPot.add(currentSize - lastSize);
				additionalPot.addPlayer(p);
				pots.add(additionalPot);
			}
			lastSize = currentSize;
		}
		
		return pots;
	}
}
