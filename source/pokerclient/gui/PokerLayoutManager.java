package pokerclient.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

public class PokerLayoutManager implements LayoutManager {
	
	private int[][] coords;
	
	private int originalWidth = 798;
	
	public PokerLayoutManager(int[][] coords) {
		this.coords = coords.clone();
	}
	
	
	public int[] getCoords(int index) {
		return coords[index];
	}
	
    public Dimension preferredLayoutSize(Container parent) {
    	return new Dimension(800, 800);
    }
    
    public Dimension minimumLayoutSize(Container parent) {
    	return new Dimension(800, 800);
    }

	@Override
	public void addLayoutComponent(String arg0, Component arg1) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void layoutContainer(Container parent) {
		 //Insets insets = parent.getInsets();
		 
        /*int maxWidth = parent.getWidth()
                       - (insets.left + insets.right);
        int maxHeight = parent.getHeight()
                        - (insets.top + insets.bottom);*/
        int nComps = parent.getComponentCount();
        double ratio = 1;
        if (parent.getWidth() != 0 && originalWidth !=0) {
        	ratio = 1.0 * parent.getWidth() / originalWidth;
        }
        originalWidth = parent.getWidth();
        if (ratio != 1) {
    		for (int i = 0; i < coords.length; i++) {
    			for (int j = 0; j < coords[i].length; j++) {
    				coords[i][j] = (int) (coords[i][j] * ratio);
    			}
    		}
        }
        for (int i = 0 ; i < nComps ; i++) {
            Component c = parent.getComponent(i);
            	//Dimension d = c.getSize();
            
            	//c.setBounds((int) (coords[i][0] * ratio), 
            		//(int) (coords[i][1] * ratio), d.width, d.height);
            if (c instanceof Rescalable) {
            	((Rescalable) (c)).rescale(ratio);
            } else {
            	c.setBounds(coords[i][0], coords[i][1], 
            			(int) (c.getBounds().width * ratio), 
            			(int) (c.getBounds().height * ratio));
        	}
	            	
	            	/*
	                 // increase x and y, if appropriate
	                if (i > 0) {
	                    if (!oneColumn) {
	                        x += previousWidth/2 + xFudge;
	                    }
	                    y += previousHeight + vgap + yFudge;
	                }

	                // If x is too large,
	                if ((!oneColumn) &&
	                    (x + d.width) >
	                    (parent.getWidth() - insets.right)) {
	                    // reduce x to a reasonable number.
	                    x = parent.getWidth()
	                        - insets.bottom - d.width;
	                }

	                // If y is too large,
	                if ((y + d.height)
	                    > (parent.getHeight() - insets.bottom)) {
	                    // do nothing.
	                    // Another choice would be to do what we do to x.
	                }

	                // Set the component's size and position.
	                c.setBounds(x, y, d.width, d.height);

	                previousWidth = d.width;
	                previousHeight = d.height; */
	        }
	}
	
	public void layoutContainer(Container parent, double ratio) {
       int nComps = parent.getComponentCount();
   		for (int i = 0; i < coords.length; i++) {
   			for (int j = 0; j < coords[i].length; j++) {
   				coords[i][j] = (int) (coords[i][j] * ratio);
   			}
   		}
       for (int i = 0 ; i < nComps ; i++) {
           Component c = parent.getComponent(i);
           	//Dimension d = c.getSize();
           
           	//c.setBounds((int) (coords[i][0] * ratio), 
           		//(int) (coords[i][1] * ratio), d.width, d.height);
           if (c instanceof Rescalable) {
           	((Rescalable) (c)).rescale(ratio);
           } else {
           	c.setBounds(coords[i][0], coords[i][1], 
           			(int) (c.getBounds().width * ratio), 
           			(int) (c.getBounds().height * ratio));
       		}
        }
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		// TODO Auto-generated method stub
	}
	

}
