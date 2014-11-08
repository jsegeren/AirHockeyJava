package airhockeyjava.simulation;

import java.util.Set;
import java.util.HashSet;

import airhockeyjava.physical.IMovingItem;

/** 
 * Class for the simulated game physics. Mocks out the detection layer for simulating game physics.
 * @author Joshua Segeren
 *
 */
public class SimulatedDetection implements IDetection {
	
	private Set<IMovingItem> movingItems = new HashSet<IMovingItem>();
	
	/* (non-Javadoc)
	 * @see airhockeyjava.simulation.IDetection#detectItemStates()
	 */
	@Override
	public void detectItemStates() {
		updateItemStates();
	}
	
	private void updateItemStates() {
		for (IMovingItem item : movingItems) {
			item.updatePosition();
			item.updateVelocity();
		}
	}
}