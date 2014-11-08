package airhockeyjava.graphics;

import java.util.Set;
import java.util.HashSet;

import airhockeyjava.physical.IMovingItem;

/** 
 * Class for simulation UI layer.
 * @author Joshua Segeren
 *
 */
public class GuiLayer implements IGuiLayer {
	
	private Set<IMovingItem> movingItems;
	
	public GuiLayer(Set<IMovingItem> movingItems) {
		this.movingItems = movingItems != null? movingItems : new HashSet<IMovingItem>();
	}
	
	/* (non-Javadoc)
	 * @see airhockeyjava.graphics.IGuiLayer#start()
	 */
	public void start() {
		// Provide loop based on desired refresh rate to render output based on item positions
	}
}