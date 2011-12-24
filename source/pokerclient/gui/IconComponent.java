package pokerclient.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;

/**
 * Wrapper for an ImageIcon that lets the user treat the image as a JComponent.
 */
public class IconComponent extends JComponent implements Rescalable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 7850355735348317856L; 
	
	/**
	 * ImageIcon to be wrapped
	 */
	private ImageIcon myIcon;
	
	/**
	 * Path to use for a default avatar. This is used if the file specified is 
	 * not found. This is done because the avatar is the only image for which 
	 * the user specifies the location - thus, it is the only one that could
	 * possibly have errors.
	 * 
	 */
	private static final String DEFAULT_AVATAR_PATH = "images/avatars/panda.png";
	
	/**
	 * Currently used path for the icon. The program reverts to this if there
	 * is an error in setting a new path.
	 */
	private String currentPath;
	
	private ImageIcon defaultSizeIcon;
	
	private static double ratio = 1;
	
	private double oldRatio = 1;
	
	/**
	 * Creates a new ImageIcon and automatically generates bounds and a 
	 * preferred size pased on the size of the image.
	 * 
	 * @param path directory of the image
	 */
	public IconComponent(String path) {
		defaultSizeIcon = new ImageIcon();
		myIcon = new ImageIcon();
		try {
			URL url = Utils.pathToURL(path);
			defaultSizeIcon = new ImageIcon(url);
			myIcon = new ImageIcon(url);
		} catch (MalformedURLException e) {
			System.err.println("Error creating URL for image.");
			e.printStackTrace();
		}
		this.setBounds(0, 0, myIcon.getIconWidth(), myIcon.getIconHeight());
		this.setPreferredSize(new Dimension(myIcon.getIconWidth(), 
				myIcon.getIconHeight()));
		currentPath = path;
		callRescale();
	}
	
	public static void setRatio(double aRatio) {
		ratio = aRatio;
	}
	
	/**
	 * Crates a new ImageIcon and creates bounds and preferred size based on
	 * a specified width and height
	 * 
	 * @param path directory of the image
	 * @param width width of the image
	 * @param height height of the image
	 */
	public IconComponent(String path, int width, int height) {
		try {
			URL url = Utils.pathToURL(path);
			defaultSizeIcon = new ImageIcon(url);
			myIcon = new ImageIcon(url);
			currentPath = path;
		} catch (NullPointerException npe) {
			defaultSizeIcon = new ImageIcon(DEFAULT_AVATAR_PATH);
			myIcon = new ImageIcon(DEFAULT_AVATAR_PATH);
			currentPath = DEFAULT_AVATAR_PATH;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.setBounds(0, 0, width, height);
		this.setPreferredSize(new Dimension(width, height));
		this.setOpaque(false);
		callRescale();
	}
	
	/**
	 * Changes the image to a new image at the specified location.
	 * 
	 * @param path new directory of the image
	 */
	public void setImage(String path) {
		if (!path.equals(currentPath)) {
				try {
					URL url = Utils.pathToURL(path);
					defaultSizeIcon = new ImageIcon(url);
					currentPath = path;
				} catch (MalformedURLException e) {
					e.printStackTrace();
					System.exit(1);
				}
			callRescale();
		} else {
			callRescale();
		}
	}
	
	private void callRescale() {
		rescale(ratio);
		/*if (ratio != 1 && ratio != oldRatio) {
			rescale(ratio);
			oldRatio = ratio;
		} else {
			System.out.println(ratio);
			myIcon.setImage(defaultSizeIcon.getImage());
		}*/
	}
	
	public void rescale(double scale) {
		try {
			//System.out.println(scale + "\t" + Arrays.toString(Thread.currentThread().getStackTrace()) + "\n\n\n\n");
			if (scale != 0) {
				myIcon.setImage(defaultSizeIcon.getImage().getScaledInstance(
						//(int) (getWidth() * scale), 
						//(int) (getHeight() * scale),
						(int) (defaultSizeIcon.getIconWidth() * ratio), 
						(int) (defaultSizeIcon.getIconHeight() * ratio), 
						Image.SCALE_DEFAULT));
				GameView.fixBounds(this, scale);
			}
		} catch (IllegalArgumentException e) {
			/*
			System.out.println("scale: " + scale);
			System.out.println("ratio: " + ratio);
			System.out.println("old width: " + getWidth());
			System.out.println("new width: " + (int) (getWidth() * scale));*/
			e.printStackTrace();
			System.out.println(currentPath);
			System.exit(-1);
		}
	}
		
	/**
	 * Sets the paint method of the JComponent to paints the icon.
	 */
	public void paint(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		myIcon.paintIcon(this, g2, 0, 0);
	}
	
	/**
	 * Returns the width of the icon.
	 */
	public int getWidth() {
		return myIcon.getIconWidth();
	}
	
	/**
	 * Returns the height of the icon.
	 */
	public int getHeight() {
		return myIcon.getIconHeight();
	}
	
}
