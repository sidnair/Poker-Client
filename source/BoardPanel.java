import java.awt.GridLayout;
import java.awt.Rectangle;
import javax.swing.JPanel;

/**
 * Board that displays its cards adjacently. Unfilled cards are represented with
 * face down cards. Arrays are used instead of array lists since the size
 * of the board is fixed. This makes the class more extendable - to use this 
 * board for a game with 7 cards, only one variable, BOARD_SIZE would need to
 * be changed. 
 * 
 * @author Sid Nair
 *
 */
public class BoardPanel extends JPanel implements Rescalable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -5171712011530957623L;
	
	/**
	 * Array of all the cards.
	 */
	private Card[] cards;
	
	/**
	 * Array of all the card icons.
	 */
	private IconComponent[] cardIcons;
	
	/**
	 * Starting x location of the board.
	 */
	private int x;
	
	/**
	 * Starting y location of the board.
	 */
	private int y;
	
	/**
	 * Number of cards that a board can hold.
	 */
	private static final int BOARD_SIZE = 5;
	
	/**
	 * Creates a new board with the specified x and y coordinates.
	 *  
	 * @param xy coordinates for the board's location.
	 */
	public BoardPanel(int[] xy) {
		x = xy[0];
		y = xy[1];
		cards = new Card[BOARD_SIZE];
		cardIcons = new IconComponent[BOARD_SIZE];
		this.setOpaque(false);
		this.setLayout(new GridLayout(1, 0));
		for (int i = 0; i < BOARD_SIZE; i++) {
			cards[i] = new Card(Card.CardStatus.HIDDEN); 
		}
		for (int i = 0; i < cards.length; i++) {
			IconComponent ic = new IconComponent(cards[i].getDir());
			this.setBounds(new Rectangle(x, y,
					(int) (this.getBounds().getWidth() + ic.getWidth()),
					(int) (this.getBounds().getHeight() + ic.getHeight())));
			cardIcons[i] = ic;
		}
		for (IconComponent ic : cardIcons) {
			this.add(ic);
		}
	}
	
	/**
	 * Updates the card at a particular index.
	 * 
	 * @param c card to add
	 * @param index index at which to add the card.
	 */
	public void setCard(Card c, int index) {
		cards[index] = c;
		cardIcons[index].setImage(c.getDir());
	}
	
	/**
	 * Clears the board.
	 */
	public void reset() {
		for (int i = 0; i < BOARD_SIZE; i++) {
			cards[i].hide();
			cardIcons[i].setImage(cards[i].getDir());
		}
	}
	
	public void rescale(double scale) {
		GameView.fixBounds(this, scale);
		for (IconComponent ic : cardIcons) {
			ic.rescale(scale);
		}
	}
		
}
