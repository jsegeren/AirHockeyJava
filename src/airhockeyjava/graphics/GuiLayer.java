package airhockeyjava.graphics;

import java.util.Set;
import java.util.HashSet;

import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Table;

/** 
 * Class for simulation UI layer.
 * @author Joshua Segeren
 *
 */
public class GuiLayer implements IGuiLayer {
	private Set<IMovingItem> movingItems;
	private Table table;
	
	public GuiLayer(Set<IMovingItem> movingItems, Table table) {
		this.movingItems = (movingItems != null)? movingItems : new HashSet<IMovingItem>();
		this.table = (table != null)? table : new Table();
	}
	
	/* (non-Javadoc)
	 * @see airhockeyjava.graphics.IGuiLayer#start()
	 */
	public void start() {
		// Provide loop based on desired refresh rate to render output based on item positions
	}
	
	// TODO should run and start be the same method?
	public void run() {}
}