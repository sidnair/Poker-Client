
import javax.swing.JOptionPane;

/**
 * This class prints out any message passed to it in a message dialog box. This 
 * avoids printing certain errors to console and provides a more user-friendly
 * way to report error messages.   
 * 
 * @author Sid Nair
 *
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
