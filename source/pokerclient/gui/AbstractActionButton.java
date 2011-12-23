package pokerclient.gui;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import javax.swing.JButton;

/**
 * An abstract button class that is meant to be extended to create raise, fold,
 * and call buttons. It provides methods to set the player associated with the
 * button and the ActionListener for the button is mostly written. When clicked,
 * the button simply calls the runNetworkedAction() method. 
 * 
 * @author Sid Nair
 *
 */
public abstract class AbstractActionButton extends JButton {
	
	/**
	 * Automatically generated serial ID. 
	 */
	private static final long serialVersionUID = 9073417153904149038L;
	
	/**
	 * Name of the player associated with the button. This is used to set and 
	 * retrieve the name of the player.
	 */
	private String myPlayerName;
	
	/**
	 * The listener associated with the button is informed of the action
	 * clicking the button represents and affects the model accordingly. 
	 */
	private PropertyChangeListener listener;
	
	/**
	 * Constructor that sets the text of a button. When clicked, the button
	 * runs the appropriate action of the player. 
	 * @param text
	 */
	public AbstractActionButton(String text, PropertyChangeListener listener) {
		this.setText(text);
		this.listener = listener;
		this.addActionListener(new
				ActionListener() {
					@Override
					public void actionPerformed(ActionEvent evt) {
						runAction();
					}
		});
	}
	
	/**
	 * Assigns a player name 
	 * 
	 * @param aPlayer player to assign.
	 */
	public void setPlayerStatus(String aPlayerName){
		myPlayerName = aPlayerName;
	}
	
	/**
	 * Returns the player of the button. This method is necessary to make the 
	 * the player do things like raise, fold, or call since these methods need
	 * to get the player name in order to create the Action that it propagates
	 * to its listener.
	 * 
	 * @return player associated with the button.
	 */
	protected String getPlayerName() {
		return myPlayerName;
	}
	
	/**
	 * Returns the listener that the button stores. Used to notify the view of
	 * actions that the player has taken.
	 * 
	 * @return the view that aggregates the button.
	 */
	protected PropertyChangeListener getListener() {
		return listener;
	}
	
	/**
	 * Executes the specific action associated with the button (fold, raise,
	 * check/call). This is an abstract method so that the rest of the 
	 * button's action listener could be written.
	 */
	public abstract void runAction();

}
