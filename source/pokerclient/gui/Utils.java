package pokerclient.gui;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class Utils {
	
	/*
	 * Given a file path, returns a URL. Needed for audio clips. Converting it
	 * to a file should make it file system agnostic.
	 */
	public static URL pathToURL(String s) throws MalformedURLException {
		return new File(s).toURI().toURL();
	}

}
