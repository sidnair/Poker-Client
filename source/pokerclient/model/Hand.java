package pokerclient.model;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Represents a Hold Em hand, which contains two cards.
 * 
 * TODO: generalize to n cards if other games are going to be supported.
 */
public class Hand implements Cloneable, Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -4638693736575885234L;
	
	/**
	 * Array of cards that makes up the hand.
	 */
	Card[] hand;
	
	/**
	 * Constructor that instantiates the hand array. By default, the hand is
	 * created as two hidden cards to indicate that the cards have not been
	 * assigned.
	 */
	public Hand() {
		this(Card.Status.HIDDEN);
	}
	
	/**
	 * Constructor for the hand that sets the cards of the hand to the 
	 * appropriate status. 
	 * 
	 * @precondition status specified is not playable - these cards must also
	 * have associated suits and values
	 * @param status status to be associated with the card.
	 */
	public Hand(Card.Status status) {
		hand = new Card[] { new Card(status), new Card(status) };
	}
	
	/**
	 * Checks if a particular card in the hand has been assigned a value. 
	 * @param index
	 * @return
	 */
	private boolean isSet(int index) {
		return hand[index] != null && hand[index].getValue() != null;
	}
	
	/**
	 * Deals a card to the current hand
	 *  
	 * @param card card to add
	 */
	public void addCard(Card card) {
		if (!isSet(0)) {
			hand[0] = card;
		} else if (!isSet(1)) {
			hand[1] = card;
		} else {
			throw new AssertionError("Hand already initialized.");
		}
	}
	
	/**
	 * Sets the card at the specified index to the card that the user passes.
	 * 
	 * @precondition the index must be less than the size of the hand minus one
	 * @param index index to which to assign the card
	 * @param card card to assign to the specified index
	 */
	public void setCard(int index, Card card) {
		hand[index] = card;
	}
	
	/**
	 * True if the hand is folded. It determines this by checking the status
	 * of each card. It is possible for each card to have a different status.
	 * If this is the case, an exception is thrown. If the user uses the class
	 * properly, this exception will never be thrown.
	 * 
	 * @return true if the hand is folded.
	 */
	public boolean isFolded() {
		if (hand[0].isFolded() == hand[1].isFolded()) {
			return hand[0].isFolded();
		} else {
			try {
				throw new Exception("Each card has a different folded status");
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			return false;
		}
	}
	
	/**
	 * Returns the first card in the hand.
	 * 
	 * @return first card in the hand
	 */
	public Card getFirst() {
		return hand[0];
	}
	
	/**
	 * Returns the second card in the hand.
	 * 
	 * @return second card in the hand
	 */
	public Card getSecond() {
		return hand[1];
	}
	
	/**
	 * Returns a String representation of the hand.
	 */
	public String toString() {
		return Arrays.toString(hand);
	}
	
	/**
	 * Clones the hand.
	 */
	public Object clone() {
		Hand cloned = new Hand();
		cloned.hand = (Card[]) hand.clone();
		return cloned;
	}
	
	/**
	 * Folds both of the cards in the hand.
	 */
	public void fold() {
		hand[0].fold();
		hand[1].fold();
	}

	/**
	 * Returns an array of both the cards in the hand.
	 * 
	 * @return the array of cards.
	 */
	public Card[] getHand() {
		return hand;
	}

}
