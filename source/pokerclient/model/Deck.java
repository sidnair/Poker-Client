package pokerclient.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Deck of cards.
 */
public class Deck implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -4288730322351067084L;

	/**
	 * Cards in the deck.
	 */
	private ArrayList<Card> cards;
	
	/**
	 * Index of the top card.
	 */
	private final static int TOP = 0;
	
	/**
	 * Default size of deck.
	 */
	private final static int DEFAULT_DECK_SIZE = 52;
	
	
	/**
	 * Constructs an empty deck of cards.
	 */
	public Deck() {
		cards = new ArrayList<Card>(DEFAULT_DECK_SIZE);
	}
	
	/**
	 * Adds a single card to the deck. This method does not check if the card
	 * is already in the deck - thus, the user is responsible for checking if
	 * duplicates exist using the isInDeck method.
	 * 
	 * @precondition the card is not already in the deck
	 * @param card Card to add.
	 */
	public void addCard(Card card) {
		assert !isInDeck(card);
		cards.add(card);
	}
	
	/**
	 * True if the card is already in the deck.
	 * 
	 * @param card card to check against the deck.
	 * @return true if the card is in the deck already
	 */
	public boolean isInDeck(Card card) {
		return cards.contains(card);
	}
	
	/**
	 * Removes a card from the deck.
	 * 
	 * @param card card to remove
	 * @return removed card
	 */
	public void removeCard(Card card) {
		assert cards.contains(card);
		cards.remove(card);
	}
	
	/**
	 * Shuffles all the cards in the deck.
	 */
	public void shuffle() {
		Collections.shuffle(cards);
	}
	
	/**
	 * Looks at top card without removing it.
	 * 
	 * @return top card
	 */
	public Card peek() {
		return cards.get(TOP);
	}
	
	/**
	 * Removes the next card and returns it.
	 * 
	 * @return top card
	 */
	public Card nextCard() {
		return cards.remove(TOP);
	}
	
	/**
	 * Recreates the deck with all cards.
	 */
	public void init() {
		cards.clear();
		for (Card.Suit s : Card.Suit.values()) {
			for (Card.Value v : Card.Value.values()) {
				cards.add(new Card(v, s));
			}
		}
		shuffle();
	}
	
	/**
	 * String representation of the deck.
	 */
	public String toString() {
		String s = "";
		for (Card c : cards) {
			s += c.toString() + "\n";
		}
		return s;
	}	

}
