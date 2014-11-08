package airhockeyjava.simulation;

import java.util.Set;
import java.util.HashSet;

import airhockeyjava.physical.IMovingItem;

/** 
 * Class for the simulated game physics. Mocks out the detection layer for simulating game physics.
 * TODO determine whether detection layer is responsible for choosing which it should update
 * or if this should be specified by the constructing/calling class (i.e. Game class).
 * @author Joshua Segeren
 *
 */
public class SimulatedDetection implements IDetection {
	
	private Set<IMovingItem> movingItems;
	
	public SimulatedDetection(Set<IMovingItem> movingItems) {
		this.movingItems = (movingItems != null)? movingItems : new HashSet<IMovingItem>();
	}
	
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