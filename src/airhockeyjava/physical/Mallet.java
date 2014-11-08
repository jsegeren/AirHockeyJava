package airhockeyjava.physical;

import airhockeyjava.util.Vector2;

/** 
 * Class to represent the mallet.
 * @author Joshua Segeren
 *
 */
public class Mallet extends MovingItem {
	
	/**
	 * Expected constructor.
	 * @param position
	 * @param velocity
	 * @param color
	 */
	public Mallet(Vector2 position, Vector2 velocity, float radius) {
		super(position, velocity, radius);
	}
}