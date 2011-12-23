package pokerclient.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JProgressBar;
import javax.swing.Timer;


/**
 * Represents a time bank timer. Has specific methods to update a linked
 * JProgressBar at appropriate intervals and a reset method to be called when
 * the player acts that sets the time to its fullest. Since the timer must
 * tick frequently to update the progress bar in a dynamic manner, it stores the
 * total amount of time the player has in terms of a number of ticks.
 *  
 * @author Sid Nair
 *
 */
public class BankTimer extends Timer implements Cloneable {
	
	/**
	 * Automatically generated serial ID.
	 */
	private static final long serialVersionUID = 2177930313913332504L;

	/**
	 * Bar linked with the timer whose value corresponds with the percentage of
	 * time in the time bank that is left. When the timer ticks, it modifies 
	 * this bar. Each time the active player changes, the view should update
	 * the bar associated with the timer. 
	 */
	private JProgressBar myBar;
		
	/**
	 * Keeps track of how many times the timer has ticked. Each time the timer
	 * ticks, the JProgressBar is updated.
	 */
	private int tickCount;
	
	/**
	 * Maximum amount of times the timer can tick before its time has expire.
	 */
	private int maxTicks;
	
	/**
	 * Number of ticks it takes to alert the player that it is his/her turn
	 * based on some method defined in the listener.
	 */
	private int alertTickCount;
	
	/**
	 * Determines the proportion of the player's total time that it takes to
	 * alert the player.
	 */
	private final static int ALERT_PROPORTION = 5;
	
	/**
	 * Delay between each tick. This determines how frequently the progress
	 * bar is updated. Different values determine the appearance of the GUI,
	 * but to not affect the total time a player has by more than a fraction
	 * of a second.
	 */
	private static final int DELAY = 30;

	/**
	 * Determines if the timer's user is the active player.
	 */
	private boolean isActive;
	
	/**
	 * Constructor that sets the total delay and listener to be associated with 
	 * the timer. 
	 * 
	 * @param totalDelay total amount of time the timer should wait before the 
	 * player auto-folds. This is used to determine maxTicks.
	 * @param listener the listener associated with the timer. The timer 
	 * notifies the listener when it expires. Since the timer should only expire
	 * if the user has not acted in time, the listener is notified when the 
	 * player should fold. A single listener is permanently associated with a
	 * given timer and does not change over time.
	 */
	public BankTimer(int totalDelay, PropertyChangeListener listener) {
		super(DELAY, null);
		maxTicks = totalDelay / DELAY;
		alertTickCount = totalDelay / (DELAY * ALERT_PROPORTION);
		addTickListener(listener);
	}
	
	/**
	 * Creates a new action listener for the timer that updates the tick count 
	 * each time the timer ticks and notifies the listener if the timer has 
	 * reach the maximum tick count.
	 * 
	 * @param pcl the listener associated with the timer that is notified if the
	 * timer expires. 
	 */
	private void addTickListener(final PropertyChangeListener pcl) {
		this.addActionListener(new 
			ActionListener() {
			
				/**
				 * This int represents the maximum number to which a bar can
				 * be set.
				 */
				private final static int FULL_BAR = 100;
			
				@Override
				/**
				 * Sets the value of the bar appropriately and notifies the
				 * listener if the maximum number of ticks have been reached. 
				 */
				public void actionPerformed(ActionEvent evt) {
					myBar.setValue((int) Math.round(FULL_BAR - 
							100.0 * tickCount / maxTicks));
					tickCount++;
					if (tickCount == alertTickCount && isActive) {
						pcl.propertyChange(new PropertyChangeEvent(
								this, GameView.TIMER_NOTIFICATION,
								new Object(), new Object()));
					}
					if (tickCount == maxTicks) {
						System.out.println("sending fold click info");
						pcl.propertyChange(new PropertyChangeEvent(
								this, GameView.TIMER_EXPIRED,
								new Object(), new Object()));
					}
				}
			});
	}
	
	/**
	 * Adds a new bar to associate with the timer. The bar's values are changed
	 * to correlate with the amount of time remaining each time the timer ticks.
	 * 
	 * @param aBar bar which the timer should modify each time it ticks.
	 */
	public void setBar(JProgressBar aBar, boolean isActive) {
		myBar = aBar;
		if (isActive) {
			myBar.setForeground(PlayerBoxComponent.ACTIVE_BAR_COLOR);
		}
		this.isActive = isActive;
	}
	
	/**
	 * Resets the timer. To be called each time a player's turn starts.
	 */
	public void reset() {
		if (myBar != null) {
			super.restart();
			super.stop();
			tickCount = 0;
			myBar.setValue(100);
			myBar.setForeground(PlayerBoxComponent.INACTIVE_BAR_COLOR);
			isActive = false;
		}
	}
	
}