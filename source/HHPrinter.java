import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * Prints the hand history to a text output.
 * 
 * @author Sid Nair
 *
 */
public class HHPrinter implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 2328912387881876453L;

	/**
	 * String describing the events of the current hand.
	 */
	private String currentHand;
	
	/**
	 * All the cumulative hh in the file.
	 */
	private String totalHH;
	
	/**
	 * Directory to which to write file.
	 */
	private String myPath;
	
	/**
	 * Constructs the printer with an empty writer.
	 */
	public HHPrinter(String pathExt) {
		myPath = "hh" + pathExt + ".txt";
		currentHand = "";
		totalHH = "";
		//steal current file so we don't overwrite the data
		try {
			BufferedReader br = new BufferedReader(new FileReader(myPath));
			boolean done = false;
			while (!done) {
				String temp = null;
				try {
					temp = br.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (temp != null) {
					totalHH += temp + "\n";
				} else {
					done = true;
				}
			}
		} catch (FileNotFoundException e) {
			//do nothing if the file hasn't been made yet
		}
	}
	
	/**
	 * Adds the passed string to the hand history of the current hand. 
	 * 
	 * @param s String to be printed.
	 */
	public void add(String s) {
		currentHand += s;
	}
	
	/**
	 * Prints the hand to disk and resets the current hand.
	 */
	public void saveHand() {
		totalHH += currentHand;
		currentHand = "";
		PrintWriter myWriter = null;
		try {
			myWriter = new PrintWriter(myPath);
			myWriter.print(totalHH);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			myWriter.close();
		}
	}

}
