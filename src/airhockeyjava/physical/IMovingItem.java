package airhockeyjava.physical;

import airhockeyjava.util.Vector2;

/**
 * Interface for all moving pieces on table.
 * @author Joshua Segeren
 *
 */
public interface IMovingItem {
	
	/**
	 * Method to check whether two moving items will collide, given their current trajectories,
	 * and expected friction.
	 * @param otherItem
	 * @return true if collision imminent
	 */
	public abstract boolean willCollide(IMovingItem otherItem);
	
	/**
	 * Method to update position of item for the next state.
	 */
	public abstract void updatePosition();
	
	/**
	 * Method to update velocity of item for the next state.
	 */
	public abstract void updateVelocity();
	
	/**
	 * Method to retrieve position of the item for the current state.
	 * @return Position the position
	 */
	public abstract Vector2 getPosition();
	
	/**
	 * Method to retrieve the velocity of the item (if available) for the current state.
	 * @return Vector2 the velocity
	 */
	public abstract Vector2 getVelocity();
	
}
