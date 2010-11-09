import java.io.Serializable;


/**
 * Represents a single card with a suit, value, and status which can be hidden
 * or folded.
 * 
 * @author Sid Nair
 *
 */
public class Card implements Serializable, Cloneable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 5916667689857495049L;

	/**
	 * Possible suits of a card.
	 */
	public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES };
	
	/**
	 * Possible values of a card.
	 */
	public enum Value { TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN,
		JACK, QUEEN, KING, ACE }
	
	/**
	 * Suit of this particular card.
	 */
	private Suit mySuit;
	
	/**
	 * Value of this particular card.
	 */
	private Value myValue;
	
	/**
	 * Possible status of a card.
	 *
	 */
	public enum CardStatus { FOLDED, HIDDEN, PLAYABLE };
	
	/**
	 * Status of this particular card. 
	 */
	private CardStatus cardStatus;
	
	/**
	 * Constructs the card with a specified value and suit. The status of the
	 * card is assumed to be playable.
	 * 
	 * @param aValue desired value of card
	 * @param aSuit desired suit of card
	 */
	public Card (Value aValue, Suit aSuit) {
		mySuit = aSuit;
		myValue = aValue;
		cardStatus = CardStatus.PLAYABLE;
	}
	
	/**
	 * Creates a folded or hidden card. This can be used to store a reference
	 * to a hidden or folded card so that these cards do not constantly need to
	 * be reinstantiated.
	 */
	public Card(CardStatus cardStatus) {
		if (cardStatus.equals(CardStatus.PLAYABLE)) {
			try {
				throw new Exception("Playable cards needs suits and values");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		this.cardStatus = cardStatus;
		mySuit = null;
		myValue = null;
	}
	
	/**
	 * Returns suit of card.
	 * 
	 * @return card's suit.
	 */
	public Suit getSuit() {
		return mySuit;
	}
	
	/**
	 * Returns value of card.
	 * 
	 * @return card's value
	 */
	public Value getValue() {
		return myValue;
	}
	
	/**
	 * Determines if two cards have the same suit and value.
	 */
	public boolean equals(Object obj) {
		Card aCard = (Card) obj;
		if (this.mySuit == aCard.mySuit && this.myValue == aCard.myValue) {
			return true;
		}
		return false;
	}
	
	/**
	 * True if the card is folded
	 * 
	 * @return true if the card is folded
	 */
	public boolean isFolded() {
		return cardStatus.equals(CardStatus.FOLDED);
	}
	
	/**
	 * String representation of the card.
	 */
	public String toString() {
		return valString() + suitString();
	}
	
	/**
	 * Converts the suit to a human-friendly string.
	 * 
	 * @return String representation of the suit of the card
	 */
	public String suitString() {
		return mySuit.toString().substring(0,1).toLowerCase();
	}
	
	/**
	 * Converts the value of the card to a human-friendly string. 
	 * 
	 * @return String representation of the value of the card
	 */
	public String valString() {
		String value = null;
		for (int i = 0; i < 8; i++) {
			if (myValue.equals(Value.values()[i])) {
				value = Integer.toString(i + 2);
			}
		}
		if (value == null) {
			switch (myValue) {
				case TEN: value = "T"; break;
				case JACK: value = "J"; break;
				case QUEEN: value = "Q"; break;
				case KING: value = "K"; break;
				case ACE: value = "A"; break;
			}
		}
		return value;
	}
	
	/**
	 * Folds the card. This changes a flag that determines the directory which
	 * the Card returns.
	 */
	public void fold() {
		cardStatus = CardStatus.FOLDED;
	}
	
	/**
	 * Returns the directory with the card image.
	 * 
	 * @return path of the image
	 */
	public String getDir() {
		if (cardStatus.equals(CardStatus.PLAYABLE)) {
			String path = "images/cards/";
			path += mySuit.toString().toLowerCase() + "/";
			path += valString() + ".jpg";
			return path;
		} else if (cardStatus.equals(CardStatus.HIDDEN)) {
			return "images/cards/hidden.jpg";
		} else { //folded
			return "images/cards/folded.jpg";
		}
	}
	
	/**
	 * Hides the card.
	 */
	public void hide() {
		this.cardStatus = CardStatus.HIDDEN;
	}
	
	/**
	 * True if the card is hidden.
	 * 
	 * @return true if the card is hidden.
	 */
	public boolean isHidden() {
		return cardStatus.equals(CardStatus.HIDDEN);
	}
	
	//comments in super.
	public Object clone() {
		Card cloned = null;
		try {
			cloned = (Card) super.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cloned;
	}
	
}
