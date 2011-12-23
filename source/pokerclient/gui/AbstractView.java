package pokerclient.gui;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import pokerclient.model.AbstractModel;

/**
 * Abstract view which follows the MVC pattern. This variation of the pattern
 * combines the view and controller. 
 * 
 * @author Sid Nair
 *
 */
public abstract class AbstractView<ConcreteModel extends AbstractModel>
		implements PropertyChangeListener  {

	/**
	 * Models for the controller.
	 */
	protected ArrayList<ConcreteModel> myModels;
	
	/**
	 * Instantiates the ArrayLists of models.
	 */
	public AbstractView() {
		myModels = new ArrayList<ConcreteModel>();
	}
	
	/**
	 * Adds a new model to the controller
	 * 
	 * @param aModel model to be added
	 */
	public void addModel(ConcreteModel aModel) {
		aModel.setPropertyChangeListener(this);
		myModels.add(aModel);
	}
	
}