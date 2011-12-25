package pokerclient.model;

import java.io.Serializable;

public class GameSettings implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -5799419573466579591L;
	

	/**
	 * Pause between dealing each card in all in situations.
	 */
	public final static int ALL_IN_PAUSE = 3000;
	
	/**
	 * Pause at the end of the street before the next card is dealt. Should be
	 * no bigger than ALL_IN_PAUSE.
	 */
	public final static int END_OF_STREET_PAUSE = 500;

	/**
	 * Pause at the end of showdown if there is more than one player in the
	 * hand.
	 */
	public final static int SHOWDOWN_PAUSE_MULTIPLE = 3000;

	/**
	 * Pause at end of showdown if just one player is involved in the hand.
	 */
	public final static int SHOWDOWN_PAUSE_SINGLE = 1000;
	
	private int timebank;
	private int bigBlind;
	private int smallBlind;
	private int ante;
	private int startingStack;
	private boolean topOff;
	
	// TODO - store meta info like game type.
	
	public GameSettings(int initialStackSize, int bigBlind, int smallBlind,
			int ante, int timebank, boolean topOff) {
		this.startingStack = initialStackSize;
		this.bigBlind = bigBlind;
		this.smallBlind = smallBlind;
		this.ante = ante;
		this.timebank = timebank;
		this.topOff = topOff;
	}
	
	// Called at end of hand.
	public void tick() {
		// TODO - update in certain circumstances, e.g. tournaments.
	}

	public int getTimebank() {
		return timebank;
	}

	public int getBigBlind() {
		return bigBlind;
	}

	public int getSmallBlind() {
		return smallBlind;
	}

	public int getAnte() {
		return ante;
	}

	public int getStartingStack() {
		return startingStack;
	}

	public boolean isTopOff() {
		return topOff;
	}

}
