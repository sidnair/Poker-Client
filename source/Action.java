import java.io.Serializable;

/**
 * An action represents a decision the user has made in the came. Actions store
 * the player's name, associated types that represent the different choices the 
 * user has made, and possibly information about the raise size.
 * 
 * @author Sid Nair
 *
 */
public class Action implements Serializable { 
		
	/**
	 * Automatically generated serial ID 
	 */
	private static final long serialVersionUID = 8205771162248992015L;

	/**
	 * An action type determines which action the user has tried to take. Each
	 * constant represents a different legal action.
	 * 
	 * @author Sid Nair
	 *
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
	 * This is a constant used to indicate that no raise size has been
	 * specified.  
	 */
	private final static int NO_SIZE_SPECIFIED = -1;
	
	/**
	 * This string contains the name of the player that made the raise. This is
	 * used to determine which player has made the raise and, consequently,
	 * which player should be allowed to make the next raise.
	 */
	private String playerName;
	
	/**
	 * Type of action which the player has taken. Used to identify the
	 * decision the player has made.
	 */
	private ActionType actionType;
	
	/**
	 * Specifies a player name actionType, but not
	 * the size to be associated with the action. Thus, it should be used 
	 * exclusively for folds, calls, and checks and not bets and raises. If the
	 * method is used improperly, an exception is thrown.  
	 * 
	 * @param playerName name of the player taking the action
	 * @param actionType type of action the player has taken. For this 
	 * constructor this should be fold, check, or call. 
	 */
	public Action(String playerName, ActionType actionType) {
		this(playerName, actionType, NO_SIZE_SPECIFIED);
		if (actionType.equals(ActionType.RAISE) || 
				actionType.equals(ActionType.BET)) {
			throw new UnsupportedOperationException("Raises and bets must" +
					"have an associated size");
		}
	}

	/**
	 * Specifies a player name, type of action, and size. This should only be
	 * called by the user if the action type is a bet or raise. The program
	 * internally calls this method with folds or raises, assigning the
	 * NO_SIZE_SPECIFIED constant to folds, checks, and calls. This process
	 * is encapsulated from the user since the NO_SIZE_SPECIFIED constant
	 * is private. 
	 * 
	 * @param playerName name of the player taking the action
	 * @param actionType type of action the player has taken. For this 
	 * constructor this should be bet or raise when called by the user.
	 * @param aSize size to which the bet or raise has been made
	 */
	public Action(String playerName, ActionType actionType, int aSize) {
		if (aSize != NO_SIZE_SPECIFIED && 
				(actionType.equals(ActionType.FOLD) || 
				actionType.equals(ActionType.CALL) ||
				actionType.equals(ActionType.CHECK))) {
			throw new UnsupportedOperationException("Folds, checks, and calls" +
				"do not have sizes associated with them.");
		}
		this.playerName = playerName;
		this.actionType = actionType;
		this.size = aSize;
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
	 * Name of player who has taken this action. This is used to inform the
	 * model which player should perform the specified action.
	 * 
	 * @return name of player who has taken this action.
	 */
	public String getPlayerName() {
		return playerName;
	}
	
	/**
	 * Returns the size associated with the action. Since only bets and raises 
	 * have sizes associated with them, this method throws an exception if
	 * the caller tries to use this for a fold, check, or call.
	 *  
	 * @return size associated with the action
	 */
	public int getSize() {
		if (size == NO_SIZE_SPECIFIED) {
			throw new UnsupportedOperationException("This type of action " +
					"has no size");
		} 
		return this.size;
	}
	
	/**
	 * Returns the String representation of the action type, which is the name
	 * of the action in all caps; this is the default toString() method of the
	 * enum class.
	 */
	public String toString() {
		return actionType.toString();
	}

}
