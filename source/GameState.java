
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Contains information about the state of the game. This is used to update the
 * view with information from the model. 
 * 
 * @author Sid Nair
 *
 */
public class GameState implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -4184116839027614077L;

	/**
	 * All the players in the game.
	 */
	private ArrayList<Player> allPlayers;
	
	/**
	 * Pots in the active hand.
	 */
	private ArrayList<Pot> pots;
	
	/**
	 * Currently acting player.
	 */
	private String activePlayerName;
	
	/**
	 * Community cards.
	 */
	private Board board;
	
	private int minBet;
	
	private int maxBet;
	
	private boolean nullActive;
	
	private boolean checkable;
	private int toCall;
	private boolean bettable;
	private int stack;
	private boolean canRaise;
	private int stableIndex;
	private ArrayList<Boolean> foldedStatuses; 
	
	/**
	 * Creates a new GameState with appropriate parameters.
	 * 
	 * @param allPlayers all the players in the game. This should be passed in
	 * a consistent order in order to keep the players at the same seats each 
	 * hand.
	 * @param activePlayer currently active player.
	 * @param pots all the pots the game model has.
	 * @param board the current board the game model is using.
	 */
	public GameState(ArrayList<Player> allPlayers, ArrayList<Pot> pots, Board board) {
		nullActive = true;
		this.allPlayers = allPlayers;
		this.allPlayers = new ArrayList<Player>();
		foldedStatuses = new ArrayList<Boolean>();
		for (Player p : allPlayers) {
			Player player = (Player) p.clone();
			foldedStatuses.add(player.getHand().isFolded());
			this.allPlayers.add(player);
		}
		this.pots = new ArrayList<Pot>();
		for (Pot p : pots) {
			this.pots.add((Pot) p.clone());
		}
		this.board = (Board) board.clone();
	}
	
	public boolean getFoldedStatus(int index) {
		return foldedStatuses.get(index);
	}
	
	public GameState(ArrayList<Player> allPlayers, Player activePlayer,
			ArrayList<Pot> pots, Board board, int stableIndex) {
		this(allPlayers, pots, board);
		this.stableIndex = stableIndex;
		nullActive = false;
		this.activePlayerName = activePlayer.getName();
		this.minBet = activePlayer.getMinBet();
		this.maxBet = activePlayer.getMaxBet();
		checkable = activePlayer.isCheckable();
		bettable = activePlayer.isBettable();
		toCall = activePlayer.getToCall();
		stack = activePlayer.getStack();
		canRaise = activePlayer.canRaise();
	}
	
	public int getStableIndex() {
		return stableIndex;
	}
	
	/**
	 * Returns the minimum legal bet/raise.
	 * 
	 * @return minimum legal bet/raise
	 */
	public int getMinBet() {
		return this.minBet;
	}
	
	public boolean getCanRaise() {
		return canRaise;
	}
	
	public boolean activeIsNull() {
		return nullActive;
	}
	
	public boolean isCheckable() {
		return checkable;
	}
	
	public int getToCall() {
		return toCall;
	}
	
	public boolean isBettable() {
		return bettable;
	}

	/**
	 * Returns the maximum legal bet/raise.
	 * 
	 * @return maximum legal bet/raise
	 */
	public int getMaxBet() {
		return this.maxBet;
	}
	
	public String getActiveName() {
		return activePlayerName;
	}
	
	public int getPlayerStack() {
		return stack;
	}
	
	/**
	 * Returns all the players at the table.
	 *  
	 * @return all the players at the table.
	 */
	public Iterator<Player> getAllPlayers() {
		return allPlayers.iterator();
	}
	
	/**
	 * Returns all the pots.
	 * 
	 * @return all the pots
	 */
	public Iterator<Pot> getPots() {
		return pots.iterator();
	}
	
	public ArrayList<Pot> getPotList() {
		return pots;
	}
	
	/**
	 * Returns the board.
	 * 
	 * @return the board.
	 */
	public Board getBoard() {
		return board;
	}

}
