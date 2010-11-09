import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Button that allows the user to call.
 * 
 * @author Sid Nair
 *
 */
public class CallButton extends AbstractActionButton {
	
	/**
	 * True if the player is allowed to check.
	 */
	private boolean checkable;
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = -3974450328405851371L;
	
	/**
	 * Creates the button with the default text "Call."
	 * 
	 * @param listener listener that the button notifies when it is clicked.
	 */
	CallButton(PropertyChangeListener listener) {
		super("Call", listener);
	}
	
	/**
	 * Updates the text of the button depending on whether its player can
	 * check or call. 
	 * 
	 * @param checkable true if the player can check
	 * @param toCall amount the player has to call to stay in the hand. 0 when
	 * checkable is true.
	 * @param stack total stack of the player. If the player can't afford to
	 * call the total raise, this number is displayed instead.
	 */
	public void updateText(boolean checkable, int toCall, int stack) {
		this.checkable = checkable;
		if (checkable) {
			this.setText("Check");
		} else {
			if (toCall <= stack) {
				this.setText("Call "  + toCall);
			} else {
				this.setText("Call " + stack);
			}
		}
	}

	//in super
	@Override
	public void runAction() {
		if (checkable) {
			super.getListener().propertyChange(new PropertyChangeEvent(this,
					GameView.PLAYER_ACTION, new String(), new Action(
							super.getPlayerName(), Action.ActionType.CHECK)));
		} else {
			super.getListener().propertyChange(new PropertyChangeEvent(this,
					GameView.PLAYER_ACTION, new String(), new Action(
							super.getPlayerName(), Action.ActionType.CALL)));
		}
	}

}
