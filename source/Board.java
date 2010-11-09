import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents board containing all the community cards.
 * 
 * @author Sid Nair
 *
 */
public class Board implements Iterable<Card>, Serializable, Cloneable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 419388270360936520L;
	
	/**
	 * ArrayList containing the cards on the board. It is instantiated to have
	 * the maximum number of cards that the board can hold. This makes the
	 * program more efficient that instantiating the board to zero. It is more
	 * convenient to use an ArrayList than an Array since it has methods
	 * to create an iterator.  
	 */
	private ArrayList<Card> myBoard;
	
	/**
	 * Constant representing the maximum number of cards which a board can hold.
	 */
	private final static int MAX_CARDS = 5;
	
	/**
	 * Constructs an empty board.
	 */
	public Board() {
		myBoard = new ArrayList<Card>(MAX_CARDS);
	}
	
	/**
	 * Adds a card to a board. Throws and exception if it tries to add a 
	 * sixth card.
	 * 
	 * @param aCard card to add
	 */
	public void addCard(Card aCard) {
		if  (myBoard.size() == MAX_CARDS) {
			try {
				throw new Exception ("Too many cards on board.");
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			myBoard.add(aCard);
		}
	}

	/**
	 * Clears the board. Used at the end of each turn.
	 */
	public void initBoard() {
		myBoard.clear();
	}
	
	/**
	 * Returns string representation of the board.
	 */
	public String toString() {
		String toReturn = "[ ";
		for (Card c : myBoard) {
			toReturn += c.toString() + ", ";
		}
		return toReturn + "]";
	}
	
	/**
	 * Returns the card at a particular index.
	 * 
	 * @param index index of card to fetch
	 * @return desired card
	 */
	public Card get(int index) {
		return myBoard.get(index);
	}
	
	/**
	 * Returns the current number of cards on the board.
	 * 
	 * @return current size of board
	 */
	public int getSize() {
		return myBoard.size();
	}

	//commented in Iterable interface
	public Iterator<Card> iterator() {
		return myBoard.iterator();
	}
	
	//commented in super
	public Object clone() {
		Board cloned = null;
		try {
			cloned = (Board) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		cloned.myBoard = new ArrayList<Card>();
		for (Card c : this.myBoard) {
			cloned.myBoard.add((Card) c.clone());
		}
		return cloned;
	}
	
}
