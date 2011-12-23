package pokerclient.gui;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import pokerclient.model.Action;

/**
 * Button that allows the user to fold.
 * 
 * @author Sid Nair
 *
 */
public class FoldButton extends AbstractActionButton {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 24348333379838484L;

	/**
	 * Constructor that sets the text of the button as "fold" and associates 
	 * the specified listener with the button.
	 * 
	 * @param listener listener that the button notifies when it is clicked.
	 */
	FoldButton(PropertyChangeListener listener) {
		super("Fold", listener);
	}

	//comments in super
	@Override
	public void runAction() {
		super.getListener().propertyChange(new PropertyChangeEvent(this,
				GameView.PLAYER_ACTION, new Object(), new Action(
						super.getPlayerName(), Action.ActionType.FOLD)));
	}
	
}
