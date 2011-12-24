package pokerclient.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Represents a player in the game.
 * 
 * @author Sid Nair
 *
 */
public class Player implements Cloneable, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7884865759477347009L;

	/**
	 * Size of the last action the player took.
	 */
	private int lastSize;
	
	/**
	 * Used to create and utilize conditions.
	 */
	private Lock myLock;
	
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
	 * Listener associated with the player; the model.  The player notifies the 
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
	private Hand myHand;
	
	/**
	 * True if player has not folded for a given street.
	 */
	private boolean inHand;
	
	private boolean bigBlind;
	
	private boolean smallBlind;
	
	/**
	 * True when player calls or folds, potentially closing the action for a 
	 * given street.
	 */
	private boolean actionClosed;
	
	/**
	 * Amount put into the pot on a given street.
	 */
	private int putInPot;
	
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
	private String myName;
	
	private boolean canAct;
	
	private Action.ActionType lastActionType;
	
	private boolean sittingOut;
	
	/**
	 * Path of the player's avatar.
	 */
	private String avatarPath;
	
	/**
	 * Constructor for the player that initializes a new hand, restacks the 
	 * player stack, assigns an ID, and assigns a name.
	 * 
	 * @param aName name to be associated with the player
	 */
	public Player(String aName, String anAvatarPath, 
			PropertyChangeListener aListener) {
		resetHand();
		this.myName = aName;
		avatarPath = anAvatarPath;
		listener = aListener;
		myLock = new ReentrantLock();
		actionTaken = myLock.newCondition();
		sittingOut = false;
	}
	
	/**
	 * Returns the hand. Pure accessor.
	 * 
	 * @return current hand
	 */
	public Hand getHand() {
		return myHand;
	}
	
	/**
	 * Returns the path of the player's avatar.
	 * @return
	 */
	public String getAvatarPath() {
		return avatarPath;
	}
	
	/**
	 * Returns player name. Pure accessor.
	 * 
	 * @return name of the player
	 */
	public String getName() {
		return myName;
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
	 * Resets the hand - deals a new hand and tops off the stack.
	 */
	public void resetHand() {
		myHand = new Hand();
		topOff();
		totalPutInPot = 0;
		resetStreet();
		currentRaise = GameModel.getBigBlind();
		bigBlind = false;
		smallBlind = false;
	}
	
	/**
	 * Tops of stack to the max buy-in if the player is shortstacked.
	 */
	private void topOff() {
		if (stack < GameModel.getInitialStacks()) {
			add(GameModel.getInitialStacks() - stack);
		}
	}
	
	/**
	 * Resets stat for each street.
	 */
	public void resetStreet() {
		inHand = true;
		putInPot = 0;
		actionClosed = false;
		acted = false;
		currentRaise = 0;
		lastSize = 0;
	}
	
	/**
	 * Determines if the player attempted to close the action.
	 * 
	 * @return true when the player called or folded last instead of raising.
	 */
	public boolean actionIsClosed() {
		return actionClosed;
	}
	
	/**
	 * Deals a new card to the player's hand.
	 * 
	 * @param aCard card to add
	 */
	public void setHand(Hand aHand) {
		myHand = aHand;
	}
	
	/**
	 * External method to pay a blind.
	 * 
	 * @param aBlind cost of blind to pay
	 * @return amount paid
	 */
	public int paySmallBlind(int aBlind) {
		smallBlind = true;
		return pay(aBlind);
	}
	
	/**
	 * Pays the big blind.
	 * 
	 * @param aBlind size of the blind
	 * 
	 * @return amount paid
	 */
	public int payBigBlind(int aBlind) {
		bigBlind = true;
		return pay(aBlind);
	}
	
	/**
	 * True if the player is the big blind.
	 * 
	 * @return boolean true if the player is the big blind.
	 */
	public boolean isBigBlind() {
		return bigBlind;
	}

	/**
	 * True if the player is the small blind.
	 * 
	 * @return boolean true if the player is the small blind.
	 */
	public boolean isSmallBlind() {
		return smallBlind;
	}
	
	/**
	 * True if the player is the either blind.
	 * 
	 * @return boolean true if the player is the either blind.
	 */
	public boolean isBlind() {
		return smallBlind || bigBlind;
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
	 * stack sizes. This is a mix of a mutator and accessor, which is okay because
	 * this is only used privately - no client will need to access it.
	 * 
	 * @param aCost amount to put into pot
	 * @return amount put into the pot
	 */
	public int pay(int aCost) {
		if (stack < aCost) {
			try {
				throw new Exception("Negative stack exception.");
			} catch (Exception e) {
				e.printStackTrace();
			}
			//TODO: actual handling makes aCost = myStack, deals with raiseSize, side pots
		}
		stack -= aCost;
		putInPot += aCost;
		totalPutInPot += aCost;
		lastSize = aCost + lastSize;
		listener.propertyChange(new PropertyChangeEvent(this, 
				GameModel.MONEY_PAID, new Object(), new Integer(aCost)));
		return aCost;
	}
	
	/**
	 * Adds money to stack - used when a player wins a pot.
	 * 
	 * @param anAmount amount to add to stack
	 */
	public void add(int anAmount) {
		stack += anAmount;
	}
	
	/**
	 * Overriden toString method that returns information about the player's
	 * name, stack, and hand.
	 */
	public String toString() {
		return myName + " " + Integer.toString(getStack()) + "\t" + myHand.toString();
	}
	
	public void updateSizing(int raiseSize, int oldRaiseSize) {
		//if the player faces a raise or this is the first time the player acts
		if (currentRaise != raiseSize || !acted) {
			if (raiseSize > 0) {
				minRaise = 2 * raiseSize - oldRaiseSize;
			} else {
				minRaise = GameModel.getBigBlind();
			}
			currentRaise = raiseSize;
			toCall = raiseSize - putInPot;
			canAct = true;
		} else {
			canAct = false;
			this.takeAction(true, inHand);
		}
	}
	
	/**
	 * Takes a turn of a player. Is an accessor and mutator.
	 * 
	 * @param raiseSize size of raise player faces
	 * @return money to be added to pot
	 */
	public void act() {
		if (canAct) {
			if (!sittingOut) {
				myLock.lock();
				try {
					actionTaken.await();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					myLock.unlock();
				}
			} else {
				fold();
			}
		}
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
	
	public void setSittingOut(boolean b) {
		sittingOut = b;
	}

	/**
	 * Notifies the player when an action has been taken.
	 */
	public void notifyPlayer() {
		myLock.lock();
		try {
			actionTaken.signalAll();
		} finally {
			myLock.unlock();
		}
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
	 * Allows a player to raise.
	 *  
	 * @param aSize size to raise to
	 * @return amount player raised by, which is how much the player needs to 
	 * pay
	 */
	public void raiseTo(int aSize, boolean betting) {
		/*if (aSize < minRaise) {
			try {
				throw new Exception ("raise is too small.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		if (isLegalRaise(aSize)) {
			currentRaise = aSize;
			if (betting) {
				printAction("bets", currentRaise);
			} else {
				printAction("raises to", currentRaise);
			}
			takeAction(false, true);
			pay(currentRaise - putInPot);
			if (betting) {
				listener.propertyChange(new PropertyChangeEvent(this, 
						GameModel.PLAYER_BET, new Object(), currentRaise));
			} else {
				listener.propertyChange(new PropertyChangeEvent(this, 
						GameModel.PLAYER_RAISED, new Object(), currentRaise));
			}
		}
	}
	
	/**
	 * Determines if the passed raise size is legal.
	 * 
	 * @param aSize raise size to check for legality
	 * @return true if the raise is legal.
	 */
	private boolean isLegalRaise(int aSize) {
		boolean overMin = aSize >= minRaise;
		boolean hasEnough = stack + putInPot >= minRaise;
		boolean putsAllIn = (stack + putInPot == aSize);
		//if (!putsAllIn && myStack == aSize) {
			//call();
		//}
		return (overMin && hasEnough) || putsAllIn;
		
	}

	/**
	 * True if the player is all in. This is the opposite of has chips, but it
	 * is its separate method so that the user doesn't have to deal with this
	 * point conceptually.
	 * 
	 * @return true if the player is all in.
	 */
	public boolean isAllIn() {
		return stack == 0;
	}
	
	/**
	 * Calls current raise, automatically checking when appropriate.
	 * 
	 * @return amount required to call
	 */
	public void call() {
		if (isCheckable()) {
			check();
		} else {
			if (stack >= toCall) { //if you can make the call normally
				printAction("calls", toCall);
				pay(toCall);
				listener.propertyChange(new PropertyChangeEvent(this, 
						GameModel.PLAYER_CALLED, new Object(), currentRaise));
			} else { //if we have to deal with side pots
				printAction("calls", stack);
				pay(stack);
				listener.propertyChange(new PropertyChangeEvent(this, 
						GameModel.PLAYER_CALLED, new Object(), currentRaise));
			}
			takeAction(true, true);
		}
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
	 * Lets a player checks.
	 * 
	 * @return amount player has to call, which is always 0
	 */
	public void check() {
		pay(0);
		printAction("checks");
		takeAction(true, true);
		listener.propertyChange(new PropertyChangeEvent(this, 
				GameModel.PLAYER_CHECKED, new Object(), currentRaise));
	}
	
	/**
	 * Folds the current hand.
	 * 
	 * @return constant representing a fold
	 */
	public void fold() {
		printAction("folds");
		takeAction(true, false);
		myHand.fold();
		listener.propertyChange(new PropertyChangeEvent(this, 
				GameModel.PLAYER_FOLDED, new Object(), this));
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
				new Object(), myName + " " + anAction + " \n"));
	}
	
	/**
	 * Converts the action the player has taken to a string and tells the 
	 * listener to reflect this change in a chat update.
	 * 
	 * @param anAction action taken
	 * @param aSize size associated with action
	 */
	public void printAction(String anAction, int aSize) {
		listener.propertyChange(new PropertyChangeEvent(this, GameModel.CHAT_UPDATE,
				new Object(), myName + " " + anAction + " " + aSize + " \n"));
	}
	
	/**
	 * Determines whether a player is in the hand. Pure accessor.
	 * 
	 * @return true when player is in hand.
	 */
	public boolean isInHand() {
		return inHand;
	}
	
	/**
	 * Returns current raise size.
	 * 
	 * @return current raise size
	 */
	public int getCurrentRaiseSize() {
		return currentRaise;
	}
	
	// Model accepts the action from the GUI
	public void acceptAction(Action action) {
		lastActionType = action.getAction();
		if (action.toString().equals(Action.ActionType.RAISE.toString())) {
			raiseTo(action.getSize(), false);
		} else if (action.toString().equals(Action.ActionType.BET.toString())) { 
			raiseTo(action.getSize(), true);
		} else if (action.toString().equals(Action.ActionType.CALL.toString())) {
			call();
		} else if (action.toString().equals(Action.ActionType.CHECK.toString())) {
			System.out.println("here");
			check();
		} else if (action.toString().equals(Action.ActionType.FOLD.toString())) {
			fold();
		}
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
		if (this.myName == p.myName) {
			return true;
		} else {
			return false;
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
	 * Calcuates and returns the maximum legal bet/raise.
	 * 
	 * @return maximum legal bet/raise.
	 */
	public int getMaxBet() {
		return this.getStack() + putInPot;
	}

	/**
	 * Calcuates and returns the minumum legal bet/raise.
	 * 
	 * @return minimum legal bet/raise.
	 */
	public int getMinBet() {
		if (minRaise < this.getMaxBet()) {
			return minRaise;
		} else {
			return getMaxBet();
		}
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
	 * True if the player is allowed to bet.
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
	 * True if the player has any chips remaining. This is the opposite of 
	 * isAllIn, but is included for the user's convenience.
	 * 
	 * @return true if the player has any chips remaining
	 */
	public boolean hasChips() {
		return stack > 0;
	}
	
	public void setLastSize(int aSize) {
		lastSize = aSize;
	}
	
	public int getLastSize() {
		return lastSize;
	}
	
	public Action.ActionType getLastActionType() {
		return lastActionType;
	}
	
	public int getPutInPot() {
		return putInPot;
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
	
}
