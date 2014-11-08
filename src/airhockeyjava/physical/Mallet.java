package airhockeyjava.physical;

import airhockeyjava.util.Vector2;
import airhockeyjava.util.Color;

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
	public Mallet(Vector2 position, Vector2 velocity, Color color) {
		super(position, velocity, color);
	}
}