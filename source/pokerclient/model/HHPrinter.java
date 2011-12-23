package pokerclient.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

/**
 * Prints the hand history to a text output.
 */
public class HHPrinter implements Serializable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 2328912387881876453L;

	/**
	 * String describing the events of the current hand. We write to file per
	 * hand.
	 */
	private String currentHand;
	
	private String filepath;
	/**
	 * Constructs the printer with an empty writer.
	 */
	public HHPrinter(String pathExt) {
		filepath = "hh" + pathExt + ".txt";
		currentHand = "";
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
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(filepath, true));
			bw.write(currentHand);
			currentHand = "";
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
