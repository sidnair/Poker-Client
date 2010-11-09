
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.JSlider;
import javax.swing.JTextField;

/**
 * A bet size slider is linked with a BetSizeTextField, modifies itself 
 * according to what is typed in the box, and provides information to the 
 * raise buttons. Since it stores a min and max, this is what limits the user
 * interface with the program to ensure that the player cannot make an 
 * illegally sized raise.
 * 
 * @author Sid Nair
 *
 */
public class BetSizeSlider extends JSlider {

	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 2779185413540072694L;

	/**
	 * Constructor that extends the super constructor, accepting an
	 * orientation, min, max, and initial value. It adds a key listener that
	 * sets its value to the value of the source of the event, the JTextField
	 * in which the user types raise sizes.
	 * 
	 * @param orientation orientation of the slider.
	 * @param min minimum legal bet/raise
	 * @param max maximum legal bet/raise
	 * @param value the initial value of the slider.
	 */
	public BetSizeSlider(int orientation, int min, int max, int value) {
		super(orientation, min, max, value);
		this.addKeyListener(new KeyAdapter() {
			/**
			 * Sets the slider's value to the value in the text field if the value 
			 * represents a legal raise size.
			 * 
			 * @param evt KeyEvent that is triggered when someone presses a key
			 */
			public void keyReleased(KeyEvent evt) {
				try {
					JTextField source = (JTextField) evt.getSource();
					int tempVal = Integer.parseInt(source.getText());
					if (tempVal <= getMaximum() && tempVal >= getMinimum()) {
						setValue(tempVal);
					}
				} catch (NumberFormatException e) {			
				}
			}
		});
	}

	/**
	 * Increases the amount on the bet slider by the specified amount. If the 
	 * amount is negative, the slider is decremented. This is more convenient
	 * for the user than calling getValue.
	 * 
	 * @param delta amount by which to change the slider's value.
	 */
	public void increment(int delta) {
		this.setValue(this.getValue() + delta);
	}
	
}
