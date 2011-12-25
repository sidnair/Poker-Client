package pokerclient.model;

import java.io.Serializable;

public class GameSettings implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -5799419573466579591L;
	
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
