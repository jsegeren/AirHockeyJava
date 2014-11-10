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
	 * Explicit setter for position.
	 * 
	 * @param newPosition
	 */
	public abstract void setPosition(Vector2 newPosition);

	/**
	 * Method to retrieve the velocity of the item (if available) for the current state.
	 * 
	 * @return Vector2 the velocity
	 */
	public abstract Vector2 getVelocity();

	/**
	 * Explicit setter for velocity
	 * 
	 * @param newVelocity
	 */
	public abstract void setVelocity(Vector2 newVelocity);

	/**
	 * Method to update velocity based on previous (known) position state information.
	 * Also updates position.
	 */
	public abstract void updatePositionAndCalculateVelocity(Vector2 newPosition);

	/**
	 * Method to retrieve the radius of the item.
	 * 
	 * @return float the radius
	 */
	public abstract float getRadius();

	/**
	 * Method to retrieve the mass of the item.
	 * @return
	 */
	public abstract float getMass();

}
