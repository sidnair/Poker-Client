import java.io.Serializable;

/**
 * Represents a Hold Em hand, which contains two cards.
 * 
 * @author Sid Nair
 *
 */
public class Hand implements Cloneable, Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -4638693736575885234L;
	
	/**
	 * Array of cards that makes up the hand.
	 */
	Card[] myHand;
	
	/**
	 * Constructor that instantiates the hand array. By default, the hand is
	 * created as two folded cards to indicate that the cards have not been
	 * assigned.
	 */
	public Hand() {
		myHand = new Card[] { new Card(Card.CardStatus.FOLDED), 
				new Card(Card.CardStatus.FOLDED) };
	}
	
	/**
	 * Constructor for the hand that sets the cards of the hand to the 
	 * appropriate status. 
	 * 
	 * @precondition status specified is not playable - these cards must also
	 * have associated suits and values
	 * @param status status to be associated with the card.
	 */
	public Hand(Card.CardStatus status) {
		myHand = new Card[] { new Card(status), new Card(status) };
	}
	
	/**
	 * Deals a card to the current hand
	 *  
	 * @param aCard card to add
	 */
	public void addCard(Card aCard) {
		try {
			if (myHand[0] == null || myHand[0].isFolded() || myHand[0].isHidden()) {
				myHand[0] = aCard;
			}
			else if (myHand[1] == null || myHand[1].isFolded() || myHand[0].isHidden()) {
				myHand[1] = aCard;
			}
			else throw new Exception ("Hand initialized already.");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
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
		myHand[index] = card;
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
		if (myHand[0].isFolded() == myHand[1].isFolded()) {
			return myHand[0].isFolded();
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
		return myHand[0];
	}
	
	/**
	 * Returns the second card in the hand.
	 * 
	 * @return second card in the hand
	 */
	public Card getSecond() {
		return myHand[1];
	}
	
	/**
	 * Returns a String representation of the hand.
	 */
	public String toString() {
		return "[" + myHand[0] + ", " + myHand[1] + "]";
	}
	
	/**
	 * Clones the hand.
	 */
	public Object clone() {
		try {
			Hand cloned = (Hand) super.clone();
			cloned.myHand = (Card[]) myHand.clone();
		}
		//Won't occur because this class is Cloneable
		catch (CloneNotSupportedException e) {
			try {
				throw new Exception("Clone fail");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Folds both of the cards in the hand.
	 */
	public void fold() {
		myHand[0].fold();
		myHand[1].fold();
	}

	/**
	 * Returns an array of both the cards in the hand.
	 * 
	 * @return the array of cards.
	 */
	public Card[] getHand() {
		return myHand;
	}

}
