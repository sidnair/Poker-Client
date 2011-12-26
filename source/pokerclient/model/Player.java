package pokerclient.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a player in the game.
 */
public class Player implements Cloneable, Serializable {
	
	/**
	 * Automatically generated ID.
	 */
	private static final long serialVersionUID = -7884865759477347009L;

	/**
	 * Used to create and utilize conditions.
	 */
	private Lock lock;
	
	/**
	 * Condition player waits for when taking action.
	 */
	private Condition actionTaken;

	/**
	 * Min raise the player can make.
	 */
	private int minRaise;
	
	/**
	 * True if it is this player's turn to act.
	 */
	private boolean active;
	
	/**
	 * True if this player has played at least once on this street.
	 */
	private boolean acted;
	
	/**
	 * Listener associated with the player; the model. The player notifies the 
	 * listener when taking an action.
	 */
	private PropertyChangeListener listener;
	
	/**
	 * Size of stack.
	 */
	private int stack;
	
	/**
	 * Current hand.
	 */
	private Hand hand;
	
	/**
	 * True if player has not folded for a given street.
	 */
	private boolean inHand;
	
	private boolean isBigBlind;
	
	private boolean isSmallBlind;
	
	/**
	 * True when player calls or folds, potentially closing the action for a 
	 * given street.
	 */
	private boolean actionClosed;
	
	/**
	 * Amount put into the pot on a given street.
	 */
	private int putInPotOnStreet;
	
	private int totalPutInPot;
	
	/**
	 * Amount needed to call to continue to stay in the pot.
	 */
	private int toCall;
	
	/**
	 * Size of the current raise in the pot for a given street.
	 */
	private int currentRaise;
	
	/**
	 * Name associated with the player.
	 */
	private String name;
	
	private boolean canAct;
	
	private Action.ActionType lastActionType;
	
	private boolean sittingOut;
	
	/**
	 * Path of the player's avatar.
	 */
	private String avatarPath;

	private GameSettings settings;
	
	/**
	 * Constructor for the player that initializes a new hand, restacks the 
	 * player stack, assigns an ID, and assigns a name.
	 * 
	 * @param name name to be associated with the player
	 */
	public Player(String name, String avatarPath,  GameSettings settings,
			PropertyChangeListener listener) {
		this.name = name;
		this.avatarPath = avatarPath;
		this.listener = listener;
		this.settings = settings;
		this.stack = settings.getStartingStack();
		lock = new ReentrantLock();
		actionTaken = lock.newCondition();
		resetHand();
	}
	
	/**
	 * Resets the hand - deals a new hand and tops off the stack.
	 */
	public void resetHand() {
		currentRaise = settings.getBigBlind();
		minRaise = settings.getBigBlind() * 2;
		hand = new Hand();
		inHand = true;
		totalPutInPot = 0;
		isBigBlind = false;
		isSmallBlind = false;
		topOff();
		resetStreet();
	}
	
	/**
	 * Resets stat for each street.
	 */
	public void resetStreet() {
		putInPotOnStreet = 0;
		currentRaise = 0;
		actionClosed = false;
		acted = false;
	}
	
	/**
	 * Tops of stack to the max buy-in if the player is shortstacked.
	 */
	private void topOff() {
		if (!settings.isTopOff()) {
			return;
		}
		
		if (stack < settings.getStartingStack()) {
			addToStack(settings.getStartingStack() - stack);
		}
	}
	
	/**
	 * External method to pay a blind.
	 * 
	 * @param aBlind cost of blind to pay
	 * @return amount paid
	 */
	public int paySmallBlind(int aBlind) {
		isSmallBlind = true;
		return pay(aBlind);
	}
	
	/**
	 * Pays the big blind.
	 * 
	 * @param aBlind size of the blind
	 * @return amount paid
	 */
	public int payBigBlind(int aBlind) {
		isBigBlind = true;
		return pay(aBlind);
	}

	/**
	 * Pays an ante.
	 * 
	 * @param anAnte size of ante
	 * 
	 * @return amount paid
	 */
	public int payAnte(int anAnte) {
		return pay(anAnte);
	}
	
	/**
	 * Puts a certain amount of money into the pot, checking for appropriate
	 * stack sizes.
	 * 
	 * @param cost amount to put into pot
	 * @return amount put into the pot
	 */
	public int pay(int cost) {
		assert stack >= cost;

		stack -= cost;
		putInPotOnStreet += cost;
		totalPutInPot += cost;
		listener.propertyChange(new PropertyChangeEvent(this,
				GameModel.MONEY_PAID, null, new Integer(cost)));
		return cost;
	}
	
	public void updateSizing(int raiseSize, int oldRaiseSize) {
		// If the player faces a raise or this is the first time the player
		// acts.
		if (currentRaise != raiseSize || !acted) {
			minRaise = Math.max(2 * raiseSize - oldRaiseSize, settings.getBigBlind());
			currentRaise = raiseSize;
			toCall = raiseSize - putInPotOnStreet;
			canAct = true;
		} else {
			canAct = false;
			this.takeAction(true, inHand);
		}
	}
	
	/**
	 * Takes a turn of a player.
	 * 
	 * @param raiseSize size of raise player faces
	 * @return money to be added to pot
	 */
	public void act() {
		if (!canAct) {
			return;
		}
		
		if (sittingOut) {
			fold();
			return;
		}
		
		lock.lock();
		try {
			actionTaken.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Notifies the player when an action has been taken.
	 */
	public void notifyPlayer() {
		lock.lock();
		try {
			actionTaken.signalAll();
		} finally {
			lock.unlock();
		}
	}
	
	public void bet(int size) {
		raiseTo(size, true);
	}
	
	public void raise(int size) {
		raiseTo(size, false);
	}

	/**
	 * Handles the logic for raising and betting, together since they are so
	 * similar.
	 *  
	 * @param raiseSize amount to raise to
	 * @param betting whether this is a raise or a bet
	 * @return amount player raised by, which is how much the player needs to 
	 * pay
	 */
	private void raiseTo(int raiseSize, boolean betting) {
		assert isLegalRaise(raiseSize);
		
		currentRaise = raiseSize;
		printAction(betting ? "bets" : "raises to", currentRaise);
		takeAction(false, true);
		pay(currentRaise - putInPotOnStreet);
		listener.propertyChange(new PropertyChangeEvent(this, 
				betting ? GameModel.PLAYER_BET : GameModel.PLAYER_RAISED,
				null, currentRaise));
	}

	/**
	 * Has the player take an action.
	 * 
	 * @param actionClosed true if the player is trying to close the action 
	 * (not raising or betting)
	 * @param inHand true if the player stays in the hand (not folding)
	 */
	public void takeAction(boolean actionClosed, boolean inHand) {
		acted = true;
		this.actionClosed = actionClosed;
		this.inHand = inHand;
		this.notifyPlayer();
		toCall = 0;
	}
	
	/**
	 * Determines if the passed raise size is legal.
	 * 
	 * @param size raise size to check for legality
	 * @return true if the raise is legal.
	 */
	private boolean isLegalRaise(int size) {
		return size >= getMinBet();
	}
	
	/**
	 * Calls current raise, automatically checking when appropriate.
	 * 
	 * @return amount required to call
	 */
	public void call() {
		assert !isCheckable();
		
		int willCall = Math.min(stack, toCall);
		pay(willCall);
		printAction("calls", willCall);
		// TODO - why passing curentRaise?
		listener.propertyChange(new PropertyChangeEvent(this, 
				GameModel.PLAYER_CALLED, null, currentRaise));
		takeAction(true, true);
	}
	
	/**
	 * Lets a player checks.
	 * 
	 * @return amount player has to call, which is always 0
	 */
	public void check() {
		assert isCheckable();
		
		printAction("checks");
		takeAction(true, true);
		listener.propertyChange(new PropertyChangeEvent(this, 
				GameModel.PLAYER_CHECKED, null, currentRaise));
	}
	
	/**
	 * Folds the current hand.
	 * 
	 * @return constant representing a fold
	 */
	public void fold() {
		printAction("folds");
		takeAction(true, false);
		hand.fold();
		listener.propertyChange(new PropertyChangeEvent(this, 
				GameModel.PLAYER_FOLDED, null, this));
	}
	
	/**
	 * Allows the model to accepts the action from the acting player (human,
	 * CPU, etc.) 
	 * @param action
	 */
	public void acceptAction(Action action) {
		lastActionType = action.getAction();
		if (action.toString().equals(Action.ActionType.RAISE.toString())) {
			raiseTo(action.getSize(), false);
		} else if (action.toString().equals(Action.ActionType.BET.toString())) { 
			raiseTo(action.getSize(), true);
		} else if (action.toString().equals(Action.ActionType.CALL.toString())) {
			call();
		} else if (action.toString().equals(Action.ActionType.CHECK.toString())) {
			check();
		} else if (action.toString().equals(Action.ActionType.FOLD.toString())) {
			fold();
		}
	}
		
	/**
	 * Sets a boolean determining if the player is closing the action.
	 *  
	 * @param b true if the player is closing the action; false if the player
	 * is not.
	 */
	public void setIsClosed(boolean b) {
		actionClosed = b;
	}
	
	/**
	 * Returns current raise size.
	 * 
	 * @return current raise size
	 */
	public int getCurrentRaiseSize() {
		return currentRaise;
	}
	
	/**
	 * True of the player has acted at least once this street.
	 * 
	 * @return true when the player has acted at least once this street.
	 */
	public boolean hasActed() {
		return acted;
	}
	
	/**
	 * Calculates and returns the maximum legal bet/raise.
	 * 
	 * @return maximum legal bet/raise.
	 */
	public int getMaxBet() {
		return this.getStack() + putInPotOnStreet;
	}

	/**
	 * Calculates and returns the minimum legal bet/raise.
	 * 
	 * @return minimum legal bet/raise.
	 */
	public int getMinBet() {
		// Make sure that the raise size is at least the minRaise if the player
		// is able to make it.
		return Math.min(minRaise, getMaxBet());
	}
	
	/**
	 * True if the player is allowed to check.
	 * 
	 * @return true if the player is allowed to check.
	 */
	public boolean isCheckable() {
		return toCall == 0;
	}
	
	/**
	 * True if the player is allowed to bet. It is false if the player would
	 * need to raise.
	 * 
	 * @return true if the player is allowed to bet.
	 */
	public boolean isBettable() {
		return currentRaise == 0;
	}
	
	/**
	 * Amount player has to call to continue in the hand.
	 * 
	 * @return amount player has to call to continue in the hand.
	 */
	public int getToCall() {
		return toCall;
	}
	
	public boolean canRaise() {
		return toCall < stack;
	}
	
	/**
	 * True if the player has chips remaining.
	 * 
	 * @return true if the player has any chips remaining
	 */
	public boolean hasChips() {
		return !this.isAllIn();
	}
	
	public Action.ActionType getLastActionType() {
		return lastActionType;
	}
	
	public int getPutInPotOnStreet() {
		return putInPotOnStreet;
	}
	
	public int getTotalPutInPot() {
		return totalPutInPot;
	}

	public void setTotalPutInPot(int totalPutInPot) {
		this.totalPutInPot = totalPutInPot;
	}

	public void setStack(int stack) {
		this.stack = stack;
	}
	
	/**
	 * Deals a new card to the player's hand.
	 * 
	 * @param aCard card to add
	 */
	public void setHand(Hand aHand) {
		hand = aHand;
	}
	
	/**
	 * Returns the player's current hand.
	 * 
	 * @return current hand
	 */
	public Hand getHand() {
		return hand;
	}
	
	/**
	 * Returns the path of the player's avatar.
	 * @return path to the player's avatar
	 */
	public String getAvatarPath() {
		return avatarPath;
	}
	
	/**
	 * Returns player name.
	 * 
	 * @return name of the player
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns size of stack.
	 * 
	 * @return stack size
	 */
	public int getStack() {
		return stack;
	}
	
	/**
	 * True if the player is the big blind.
	 * 
	 * @return boolean true if the player is the big blind.
	 */
	public boolean isBigBlind() {
		return isBigBlind;
	}

	/**
	 * True if the player is the small blind.
	 * 
	 * @return boolean true if the player is the small blind.
	 */
	public boolean isSmallBlind() {
		return isSmallBlind;
	}
	
	/**
	 * True if the player is the either blind.
	 * 
	 * @return boolean true if the player is the either blind.
	 */
	public boolean isBlind() {
		return isSmallBlind || isBigBlind;
	}
	
	/**
	 * Determines if the player attempted to close the action.
	 * 
	 * @return true when the player called or folded last instead of raising.
	 */
	public boolean isActionClosed() {
		return actionClosed;
	}

	/**
	 * Adds money to stack - used when a player wins a pot.
	 * 
	 * @param amount amount to add to stack
	 */
	public void addToStack(int amount) {
		stack += amount;
	}

	public String toString() {
		return name + ": " + getStack() + "\t" + hand.toString();
	}

	/**
	 * True if the player is active.
	 * 
	 * @return true if the player is active.
	 */
	public boolean isActive() {
		return active;
	}
	
	/**
	 * Sets the player as active or inactive.
	 * 
	 * @param b true if the player should be active; false if the player should
	 * not be active.
	 */
	public void setActive(boolean b) {
		active = b;
	}
	
	
	public void sitOut() {
		sittingOut = true;
	}

	/**
	 * Clones a player.
	 */
	public Object clone() {
		try {
			Player cloned = (Player) super.clone();
			//cloned.myHand = (Hand) myHand.clone();
			return cloned;
			}
			//Won't occur because this class is Cloneable
			catch (CloneNotSupportedException e) {
				return null;
			}
	}

	/**
	 * Checks for equality.
	 */
	public boolean equals(Object o) {
		Player p = (Player) o;
		return this.name.equals(p.name);
	}
	
	/**
	 * Determines whether a player is in the hand.
	 * 
	 * @return true when player is in hand.
	 */
	public boolean isInHand() {
		return inHand;
	}

	/**
	 * Converts the action the player has taken to a string and tells the 
	 * listener to reflect this change in a chat update. This method is used
	 * for actions that do not have a size associated with them.
	 * 
	 * @param anAction action taken
	 */
	public void printAction(String anAction) {
		listener.propertyChange(new PropertyChangeEvent(this, GameModel.CHAT_UPDATE,
				null, name + " " + anAction + " \n"));
	}
	
	/**
	 * Converts the action the player has taken to a string and tells the 
	 * listener to reflect this change in a chat update.
	 * 
	 * @param anAction action taken
	 * @param aSize size associated with action
	 */
	public void printAction(String anAction, int aSize) {
		listener.propertyChange(
				new PropertyChangeEvent(this,
						GameModel.CHAT_UPDATE, null,
						name + " " + anAction + " " + aSize + " \n"));
	}

	/**
	 * True if the player is all in.
	 * 
	 * @return true if the player is all in.
	 */
	public boolean isAllIn() {
		return stack == 0;
	}
	
}
