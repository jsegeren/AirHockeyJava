package airhockeyjava.physical;

import airhockeyjava.util.Vector2;

/** 
 * Class to represent the puck.
 * @author Joshua Segeren
 *
 */
public class Puck extends MovingItem {
	
	/**
	 * Expected constructor.
	 * @param position
	 * @param velocity
	 * @param color
	 */
	public Puck(Vector2 position, Vector2 velocity, float radius) {
		super(position, velocity, radius);
	}
}