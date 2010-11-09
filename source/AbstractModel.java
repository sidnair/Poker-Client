import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Abstract model which follows the MVC pattern.
 * 
 * @author Sid Nair
 *
 */
public abstract class AbstractModel {

    private PropertyChangeListener listener;
    
    /**
     * Constructs the model by instantiating a PropertyChangeSupport. 
     */
    public AbstractModel() {
    }
    
	/**
	 * Adds a listener to notify of property changes.
	 * 
	 * @param listener listener to notify of any property changes
	 */
	protected void setPropertyChangeListener(PropertyChangeListener listener) {
            this.listener = listener;
    }

	/**
	 * Fires a property change report to its listener. The method specifies the
	 * source of the event as the model itself and requests a String identifier,
	 * old value, and new value.
	 * 
	 * @param identifier string to indicate which property has been changed. 
	 * Typically, this will be a constant String in the listener.
	 * @param oldValue original value of the object
	 * @param newValue new value of the object
	 */
	protected void firePropertyChange(String identifier, Object oldValue, 
			Object newValue) {
            try {
		listener.propertyChange(new PropertyChangeEvent(this,
                        identifier, oldValue, newValue));
            } catch (NullPointerException npe) {
                
            }
	}

}
