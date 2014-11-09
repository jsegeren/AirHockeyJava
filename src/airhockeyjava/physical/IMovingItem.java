package airhockeyjava.physical;

import airhockeyjava.util.Vector2;

/**
 * Interface for all moving pieces on table.
 * 
 * @author Joshua Segeren
 *
 */
public interface IMovingItem {
	/**
	 * Method to retrieve position of the item for the current state.
	 * 
	 * @return Position the position
	 */
	public abstract Vector2 getPosition();

	/**
	 * Method to retrieve the velocity of the item (if available) for the current state.
	 * 
	 * @return Vector2 the velocity
	 */
	public abstract Vector2 getVelocity();

	/**
	 * Method to retrieve the radius of the item.
	 * 
	 * @return float the radius
	 */
	public abstract float getRadius();

}
