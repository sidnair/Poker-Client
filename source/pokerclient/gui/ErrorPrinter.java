package pokerclient.gui;

import javax.swing.JOptionPane;

/**
 * User-friendly way to display errors.
 */
public class ErrorPrinter {
	
	/**
	 * Creates a message dialog with the error message.
	 * 
	 * @param errorMessage error message to be printed.
	 */
	public static void printError(String errorMessage) {
		JOptionPane.showMessageDialog(null, errorMessage);
	}

}
