package pokerclient.model;

import java.io.Serializable;

import pokerclient.gui.GameView;

/**
 * Represents a single card with a suit, value, and status which can be hidden
 * or folded.
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
	private Suit suit;
	
	/**
	 * Value of this particular card.
	 */
	private Value value;
	
	/**
	 * Possible status of a card.
	 */
	public enum Status { FOLDED, HIDDEN, VISIBLE };
	
	/**
	 * Status of this particular card. 
	 */
	private Status status;
	
	/**
	 * Constructs the card with a specified value and suit. The status of the
	 * card is VISIBLE by default.
	 * 
	 * @param value desired value of card
	 * @param suit desired suit of card
	 * @param status status of the card
	 */
	public Card(Value value, Suit suit, Status status) {
		this.suit = suit;
		this.value = value;
		this.status = status;
	}
	
	/**
	 * Constructs the card with a specified value and suit. The status of the
	 * card is VISIBLE by default.
	 * 
	 * @param value desired value of card
	 * @param suit desired suit of card
	 */
	public Card(Value value, Suit suit) {
		this(value, suit, Status.VISIBLE);
	}
	
	/**
	 * Creates a folded or hidden card. This can be used to store a reference
	 * to a hidden or folded card so that these cards do not constantly need to
	 * be reinstantiated.
	 * 
	 * @param cardStatus status of the card
	 */
	public Card(Status cardStatus) {
		this(null, null, cardStatus);
		assert !cardStatus.equals(Status.VISIBLE);
	}
	
	/**
	 * Returns suit of card.
	 * 
	 * @return card's suit.
	 */
	public Suit getSuit() {
		return suit;
	}
	
	/**
	 * Returns value of card.
	 * 
	 * @return card's value
	 */
	public Value getValue() {
		return value;
	}
	
	/**
	 * Determines if two cards have the same suit and value.
	 */
	public boolean equals(Object obj) {
		Card other = (Card) obj;
		if (this.suit == other.suit && this.value == other.value) {
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
		return status.equals(Status.FOLDED);
	}
	
	/**
	 * True if the card is hidden.
	 * 
	 * @return true if the card is hidden.
	 */
	public boolean isHidden() {
		return status.equals(Status.HIDDEN);
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
	private String suitString() {
		return suit.toString().substring(0,1).toLowerCase();
	}
	
	/**
	 * Converts the value of the card to a human-friendly string. 
	 * 
	 * @return String representation of the value of the card
	 */
	private String valString() {
		String strValue = null;
		for (int i = 0; i < 8; i++) {
			if (value.equals(Value.values()[i])) {
				strValue = Integer.toString(i + 2);
			}
		}
		if (strValue == null) {
			switch (value) {
				case TEN:
					strValue = "T";
					break;
				case JACK:
					strValue = "J";
					break;
				case QUEEN:
					strValue = "Q";
					break;
				case KING:
					strValue = "K";
					break;
				case ACE:
					strValue = "A";
					break;
			}
		}
		
		assert strValue != null;
		
		return strValue;
	}
	
	/**
	 * Returns the directory with the card image.
	 * 
	 * @return path of the image
	 */	
	public String getDir() {
		String path = GameView.MEDIA_ROOT + "images/cards/";
		switch (status) {
			case VISIBLE:
				path += suit.toString().toLowerCase() + "/";
				path += valString() + ".jpg";
				System.out.println(path);
				break;
			case HIDDEN:
				path += "hidden.jpg";
				break;
			case FOLDED:
				path += "folded.jpg";
				break;
		}
		return path;
	}
	
	/**
	 * Folds the card.
	 */
	public void fold() {
		status = Status.FOLDED;
	}
	
	/**
	 * Hides the card.
	 */
	public void hide() {
		this.status = Status.HIDDEN;
	}
	
	@Override
	public Object clone() {
		return new Card(this.value, this.suit, this.status);
	}
	
}
