
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Deck of 52 cards.
 * 
 * @author Sid Nair
 *
 */
public class Deck implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -4288730322351067084L;

	/**
	 * ArrayList of cards.
	 */
	private ArrayList<Card> myCards;
	
	/**
	 * Index of the top card.
	 */
	private final static int TOP = 0;
	
	private final static int DEFAULT_DECK_SIZE = 52;
	
	
	/**
	 * Constructs an empty deck of cards.
	 */
	public Deck() {
		myCards = new ArrayList<Card>(DEFAULT_DECK_SIZE);
	}
	
	/**
	 * Adds a single card to the deck. This method does not check if the card
	 * is already in the deck - thus, the user is responsible for checking if
	 * duplicates exist using the isInDeck method.
	 * 
	 * @precondition the card is not already in the deck
	 * @param aCard Card to add.
	 */
	public void addCard(Card aCard) {
		/*try {
			for (int i = 0; i < myCards.size(); i++) {
				if (myCards.get(i).equals(aCard)) {
					throw new Exception("Card already in deck.");
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}*/
		myCards.add(aCard);
	}
	
	/**
	 * True if the card is already in the deck.
	 * 
	 * @param card card to check against the deck.
	 * @return true if the card is in the deck already
	 */
	public boolean isInDeck(Card card) {
		for (Card c : myCards) {
			if (c.equals(card)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds an array of cards.
	 * 
	 * @param cards array of cards to add
	 */
	public void addCards(Card[] cards) {
		for (Card c : cards) {
			addCard(c);
		}
	}
	
	/**
	 * Removes a card from the deck.
	 * 
	 * @param aCard card to remove
	 * @return removed card
	 */
	public Card removeCard(Card aCard) {
		try {
			if (myCards.remove(aCard)) {
				return aCard;
			} else {
				throw new Exception("Card not in deck.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.exit(1);
		}
		return null;
	}
	
	/**
	 * Shuffles all the cards in the deck. This uses a default method in 
	 * Collections. This method is provided to make the program more OO - the
	 * user should just be able to call shuffle without knowing how the deck
	 * is stored.
	 */
	public void shuffle() {
		Collections.shuffle(myCards);
		
		//old custom shuffle method
		/*ArrayList<Card> temp = new ArrayList<Card>();
		while (myCards.size() > 0) {
			int index = (int) Math.round((myCards.size() - 1) * Math.random());
			temp.add(myCards.get(index));
			myCards.remove(index);
		}
		myCards = temp;*/
	}
	
	/**
	 * Looks at top card without removing it.
	 * 
	 * @return top card
	 */
	public Card peek() {
		return myCards.get(TOP);
	}
	
	/**
	 * Removes the next card and returns it.
	 * 
	 * @return top card
	 */
	public Card nextCard() {
		return myCards.remove(TOP);
	}
	
	/**
	 * Recreates the deck with all 52 cards
	 */
	public void init() {
		myCards.clear();
		for (Card.Suit s : Card.Suit.values()) {
			for (Card.Value v : Card.Value.values()) {
				myCards.add(new Card(v, s));
			}
		}
		shuffle();
	}
	
	/**
	 * String representation of the deck.
	 */
	public String toString() {
		String toReturn = "";
		for (Card c : myCards) {
			toReturn += c.toString() + "\n";
		}
		return toReturn;
	}
	

}
