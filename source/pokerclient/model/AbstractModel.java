package pokerclient.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Abstract model which follows the MVC pattern.
 */
public abstract class AbstractModel {

    private PropertyChangeListener listener;
    
	/**
	 * Adds a listener to notify of property changes.
	 * 
	 * @param listener listener to notify of any property changes
	 */
	public void setPropertyChangeListener(PropertyChangeListener listener) {
            this.listener = listener;
    }

	/**
	 * Fires a property change report to its listener. The source of the event
	 * is the model, and the identifier, oldValue, and newValue are specified
	 * as parameters.
	 * 
	 * @param identifier string to indicate which property has been changed. 
	 * Typically, this will be a constant in the listener.
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
