package pokerclient.gui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pokerclient.model.Action;

/**
 * Button that allows the user to raise. It tries to prevent illegal requests
 * by not talking to the player if the size is determined illegal. It determines
 * legality using the min and max values of the BetSizeSlider.
 * 
 * @author Sid Nair
 *
 */
public class RaiseButton extends AbstractActionButton implements ChangeListener {
	
	/**
	 * Automatically generated serial ID. 
	 */
	private static final long serialVersionUID = 6941033609101878297L;
	
	/**
	 * Current raise size to be used and displayed.
	 */
	private int raiseSize;
	
	/**
	 * True if the player can raise (as opposed to just bet).
	 */
	private boolean raisable;
	
	private boolean tooShort;
	
	/**
	 * Constructor that automatically sets the text to bet.
	 */
	RaiseButton(PropertyChangeListener listener) {
		super("Bet", listener);
	}

	@Override
	public void runAction() {
		if (raisable) {
			super.getListener().propertyChange(new PropertyChangeEvent(this,
					GameView.PLAYER_ACTION, null, new Action(
							super.getPlayerName(), Action.ActionType.RAISE, 
							raiseSize)));
		} else {
			super.getListener().propertyChange(new PropertyChangeEvent(this,
					GameView.PLAYER_ACTION, null,
					new Action(super.getPlayerName(), Action.ActionType.BET, 
							raiseSize)));
		}
	}
	
	/**
	 * Updates the text of the button and disables it if necessary.
	 * 
	 * @param bettable true if the player is able to bet and not raise
	 * @param tooShort true if the player is too short to raise or bet.
	 */
	public void updateText(boolean bettable, boolean tooShort) {
		this.tooShort = tooShort;
		updateText(bettable);
	}
	
	/**
	 * Updates the text of the button.
	 * 
	 * @param bettable true if the player is able to bet and not raise
	 */
	public void updateText(boolean bettable) {
		this.raisable = !bettable;
		if (this.raisable) {
			this.setText("Raise " + this.raiseSize);
		} else {
			this.setText("Bet " + this.raiseSize);
		}
		if (tooShort) {
			this.setEnabled(false);
		} else {
			this.setEnabled(true);
		}
	}

	/**
	 * Updates the min, max, and current raise size based on the values of the 
	 * slider.
	 */
	public void stateChanged(ChangeEvent evt) {
		BetSizeSlider source = (BetSizeSlider) evt.getSource();
		this.raiseSize = source.getValue();
		updateText(!raisable);
	}
}
