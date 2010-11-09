import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Text field in which the user enters bet sizes. It updates to match the value
 * of the slider if the slider is dragged.
 * 
 * @author Sid Nair
 *
 */
public class BetSizeTextField extends JTextField implements ChangeListener {

	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 5615555119594329871L;
	
	/**
	 * Size of the bet or raise to be made.
	 */
	private String size;
	
	/**
	 * Sets the value of the text box to the value of the bet slider.
	 */
	public void stateChanged(ChangeEvent evt) {
		BetSizeSlider source = (BetSizeSlider) evt.getSource();
		this.size = Integer.toString(source.getValue());
		this.setText(size);
	}

}
