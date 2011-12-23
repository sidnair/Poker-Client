package pokerclient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Represents the board with the community cards.
 */
public class Board implements Iterable<Card>, Serializable, Cloneable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 419388270360936520L;
	
	/**
	 * The cards on the board.
	 */
	private ArrayList<Card> cards;
	
	/**
	 * The maximum number of cards which a board can hold.
	 */
	private final static int MAX_CARDS = 5;
	
	/**
	 * Constructs an empty board.
	 */
	public Board() {
		cards = new ArrayList<Card>(MAX_CARDS);
	}
	
	/**
	 * Adds a card to a board. Throws an exception if it tries to add a 
	 * sixth card.
	 * 
	 * @param aCard card to add
	 */
	public void addCard(Card aCard) {
		assert cards.size() < MAX_CARDS;
		cards.add(aCard);
	}

	/**
	 * Initializes the board at the end of a turn.
	 */
	public void initBoard() {
		cards.clear();
	}

	@Override
	public String toString() {
		return cards.toString();
	}
	
	/**
	 * Returns the card at a particular index.
	 * 
	 * @param index index of card to fetch
	 * @return desired card
	 */
	public Card get(int index) {
		return cards.get(index);
	}
	
	/**
	 * Returns the current number of cards on the board.
	 * 
	 * @return current size of board
	 */
	public int getCardCount() {
		return cards.size();
	}

	@Override
	public Iterator<Card> iterator() {
		return cards.iterator();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		Board cloned = new Board();
		cloned.cards = (ArrayList<Card>) cards.clone();
		return cloned;
	}
	
}
