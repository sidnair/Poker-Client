package pokerclient.model;

import java.io.Serializable;

/**
 * An action represents a decision the user has made in the game. Actions store
 * the player's name, associated types that represent the different choices the 
 * user has made, and possibly information about the raise size.
 */
public class Action implements Serializable { 
		
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 8205771162248992015L;

	/**
	 * An action type determines which action the user has tried to take.
	 */
	public enum ActionType implements Serializable {
		FOLD, CHECK, CALL, BET, RAISE;
	}
		
	/**
	 * If a raise has been made, this size corresponds to the size to which the
	 * raise should be made.
	 */
	private int size;
	
	/**
	 * This string contains the name of the player that made the raise. This is
	 * used to determine which player has made the raise and, consequently,
	 * which player should be allowed to make the next raise.
	 */
	private String playerName;
	
	/**
	 * Type of action which the player has taken.
	 */
	private ActionType actionType;
	
	/**
	 * Specifies a player name actionType, but not the size to be associated
	 * with the action. Thus, it should be used exclusively for folds, calls,
	 * and checks and not bets and raises. If the method is used improperly, an
	 * exception is thrown.  
	 * 
	 * @param playerName name of the player taking the action
	 * @param actionType type of action the player has taken. For this 
	 * constructor this should be fold, check, or call. 
	 */
	public Action(String playerName, ActionType actionType) {
		this(playerName, actionType, 0);
		assert !hasRaiseSize();
	}

	/**
	 * Creates a bet or raise action.
	 * 
	 * @param playerName name of the player taking the action
	 * @param actionType type of action the player has taken. For this 
	 * constructor this should be bet or raise when called by the user.
	 * @param aSize size to which the bet or raise has been made
	 */
	public Action(String playerName, ActionType actionType, int aSize) {
		this.playerName = playerName;
		this.actionType = actionType;
		this.size = aSize;
	}
	
	private boolean hasRaiseSize() {
		return !actionType.equals(ActionType.RAISE) &&
				!actionType.equals(ActionType.BET);
	}
	
	/**
	 * Returns the type of action associated with the Action object.
	 * 
	 * @return type of action associated with the Action object
	 */
	public Action.ActionType getAction() {
		return actionType;
	}
	
	/**
	 * Name of player who has taken this action.
	 * 
	 * @return name of player who has taken this action.
	 */
	public String getPlayerName() {
		return playerName;
	}
	
	/**
	 * Returns the size associated with the action. Only a valid call for
	 * bets and raises.
	 *  
	 * @return size associated with the action
	 */
	public int getSize() {
		assert hasRaiseSize();
		return this.size;
	}
	
	@Override
	public String toString() {
		return actionType.toString();
	}

}
