package pokerclient.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import pokerclient.model.Card;
import pokerclient.model.Hand;

/**
 * JComponent that draws a player's avatar, cards, and label with stack and 
 * name.
 * 
 * @author Sid Nair
 *
 */
public class PlayerBoxComponent extends JComponent implements Rescalable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 8230929532964465184L;
	
	/**
	 * Name of the player.
	 */
	private JLabel name;
	
	/**
	 * Stack size of the player.
	 */
	private JLabel stack;
	
	/**
	 * Bar that models the timer of the player.
	 */
	private JProgressBar timeBankBar;
	
	/**
	 * Avatar of the player.
	 */
	private IconComponent avatar;
	
	/**
	 * Cards of the player.
	 */
	private JPanel cards;
	
	/**
	 * Color to use for the label background.
	 */
	private static final int SILVER = 245;
	
	/**
	 * Maximum width to ues for the components.
	 */
	private static final int MAX_WIDTH = 75;
	
	/**
	 * Icon of the first card.
	 */
	private IconComponent card1;
	
	/**
	 * Icon of the second card.
	 */
	private IconComponent card2;
	
	/**
	 * Color used for a bar when it is not this player's turn.
	 */
	public static final Color INACTIVE_BAR_COLOR = new Color(0, 204, 255);
	
	/**
	 * Color used for a bar when it is this player's turn.
	 */
	public static final Color ACTIVE_BAR_COLOR = new Color(255, 0, 0);
	
	/**
	 * Folded hand - used when the hand has been folded. This is stored so that
	 * the blank hand doesn't need to be generated every time the image of the
	 * hand must be redrawn.
	 */
	private final static Hand foldedHand = new Hand(Card.Status.FOLDED);
	
	/**
	 * Blank hand - used when this shouldn't be visible. This is stored so that
	 * the blank hand doesn't need to be generated every time the image of the
	 * hand must be redrawn.
	 */
	private final static Hand hiddenHand = new Hand(Card.Status.HIDDEN);
	
	/**
	 * True if the player is currently active. Used to avoid redrawing boxes
	 * when the player is already active.
	 */
	private boolean activeStatus;
	
	
	/**
	 * Constructor that instantiates the JPanel with parameters determining
	 * its location and appearance.
	 * 
	 * @param x default x-coordinate
	 * @param y default y-coordinate
	 * @param avatarPath path to be used to find the avatar
	 * @param aHand hand associated with the player
	 * @param aName name of player
	 * @param isActive true if the player is making a decision
	 * @param isActive true if the player is making a decision
	 * @param isVisible true if the user can view the player's hand
	 * @param isFolded true if the player's hand has been folded
	 * 
	 */
	public PlayerBoxComponent(int x, int y, String avatarPath, Hand aHand, 
			String aName, boolean isActive, boolean isVisible, boolean isFolded, 
			String aStack) {
		this.setBackground(Color.CYAN);
		this.setOpaque(true);
		this.name = new JLabel(aName);
		this.stack = new JLabel(aStack);
		this.name.setOpaque(true);
		this.stack.setOpaque(true);
		this.name.setBounds(0, 0, MAX_WIDTH, (int) (name.getPreferredSize().getHeight()));
		this.stack.setBounds(0, 0, MAX_WIDTH, (int) (stack.getPreferredSize().getHeight()));
		avatar = new IconComponent(avatarPath, MAX_WIDTH, MAX_WIDTH);
		timeBankBar = new JProgressBar();
		timeBankBar.setValue(100);
		timeBankBar.setForeground(INACTIVE_BAR_COLOR);
		initCards();
		setHand(aHand, isVisible, isFolded);
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		addAll();
		cards.setOpaque(false);
		activeStatus = !isActive;
		setActive(isActive);
		stack.setFont(new Font(stack.getFont().getFontName(), 
				Font.BOLD, stack.getFont().getSize()));
		setCoords(new int[] { x, y });
	}
	
	public void rescale(double scale) {
		this.removeAll();
		GameView.fixBounds(this, scale);
		avatar.rescale(scale);
		GameView.fixBounds(name, scale);
		GameView.fixBounds(stack, scale);
		GameView.fixBounds(timeBankBar, scale);
		for (Component c : cards.getComponents()) {
			((IconComponent) c).rescale(scale);
		}
		this.addAll();
	}
	
	public void removeAll() {
		this.remove(avatar);	
		this.remove(name);
		this.remove(stack);
		//this.remove(infoLabel);
		this.remove(timeBankBar);
		this.remove(cards);
	}
	
	private void addAll() {
		this.add(avatar);	
		//this.add(infoLabel);
		this.add(name);
		this.add(stack);
		this.add(timeBankBar);
		this.add(cards);
	}
	
	/**
	 * Sets the initial coordinates of the player.
	 * 
	 * @param xy coordinates of the player.
	 */
	private void setCoords(int[] xy) {
		this.setBounds(xy[0], xy[1], (int) MAX_WIDTH, 
				(int) this.getPreferredSize().getHeight());
	}
	
	/**
	 * Updates the view if the player is or is not active.
	 * 
	 * @param isActive true if the player is making a decision.
	 */
	public void setActive(boolean isActive) {
		if (activeStatus != isActive) {
			initTextField(name, isActive);
			initTextField(stack, isActive);
			activeStatus = isActive;
		}
	}
	
	/**
	 * Instantiates or updates the player's hand.
	 * 
	 * @param aHand hand to display
	 * @param visible true if the user can see this hand
	 * @param folded true if the hand has been folded
	 */
	public void setHand(Hand aHand, boolean visible, boolean folded) {
		if (folded) {
			aHand = foldedHand;
		} else if (!visible) {
			aHand = hiddenHand;
		}
		try {
			if (card1 != null) {
				//card1.setImage(aHand.getFirst().getDir());
				this.updateImage(card1, aHand.getFirst().getDir());
			} else {
				card1 = new IconComponent(aHand.getFirst().getDir());
				cards.add(card1);
			}
			if (card2 != null) {
				this.updateImage(card2, aHand.getSecond().getDir());
			} else {
				card2 = new IconComponent(aHand.getSecond().getDir());
				cards.add(card2);
			}
		} catch (NullPointerException e) {
			//does nothing when the hand has been folded and the cards are null
		}
	}
	
	private void updateImage(IconComponent component, String path) {
		//Rectangle bounds = component.getBounds();
		component.setImage(path);
		//component.rescale(1.0 * bounds.width / component.getBounds().width);
	}
	
	/**
	 * Updates the avatar of the player.
	 * 
	 * @param path directory of the avatar to use
	 */
	public void setAvatar(String path) {
		//the image won't update if it is already being used, so this check
		//doesn't have to be done here
		updateImage(avatar, path);
		/*
		Rectangle bounds = avatar.getBounds();
		avatar.setImage(path);
		avatar.rescale(bounds.width / avatar.getBounds().width);*/
	}
	
	/**
	 * Initializes the cards, selecting the layout and orientation.
	 */
	public void initCards() {
		cards = new JPanel();
		cards.setLayout(new GridLayout(1, 2));
		cards.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
	}
	
	/**
	 * Formats the passed text field that becomes the stack size or player name
	 * with appropriate background colors.
	 * 
	 * @param tf text field to set up.
	 * @param isActive true if the player is active. This determines the
	 * color used.
	 */
	private void initTextField(JLabel tf, boolean isActive) {
		SimpleAttributeSet attribs = new SimpleAttributeSet();
		StyleConstants.setAlignment(attribs, StyleConstants.ALIGN_CENTER);
		if (isActive) {
			tf.setForeground(Color.BLACK);
			//tf.setDisabledTextColor(Color.BLACK);
			tf.setBackground(new Color(SILVER, SILVER, SILVER));
		} else {
			tf.setForeground(Color.WHITE);
			tf.setBackground(Color.DARK_GRAY);
			//tf.setDisabledTextColor(Color.WHITE);
		}
		tf.setEnabled(false);
		tf.setHorizontalAlignment(JTextField.CENTER);
		tf.setBorder(null);
	}
	
	public JProgressBar getBar() {
		return timeBankBar;
	}
	
	/**
	 * Updates the player name.
	 * 
	 * @param aName name to use for the player.
	 */
	public void setPlayerName(String aName) {
		if (aName != name.getText()) {
			name.setText(aName);
		}
	}
	
	/**
	 * Updates the player stack.
	 * 
	 * @param aStack stack to use for player.
	 */
	public void setStack(String aStack) {
		if (aStack != stack.getText()) {
			stack.setText(aStack);
		}
	}
	
	/**
	 * Updates the location of the player.
	 * 
	 * @param x x-coordinate of location
	 * @param y y-coordinate of location
	 */
	/*public void setLocation(int x, int y) {
		this.setBounds(x, y, (int) MAX_WIDTH, 
				(int) this.getPreferredSize().getHeight());
	}*/

}
