package airhockeyjava.simulation;

import airhockeyjava.physical.IMovingItem;

/**
 * Interface for the detection layer which determines and updates states of moving items.
 * 
 * @author Joshua Segeren
 *
 */
public interface IDetection {

	/**
	 * Method to either calculate or visually detect, and then update item states.
	 */
	public abstract void detectAndUpdateItemStates(float deltaTime);

	/**
	 * Method to check whether two moving items will collide, given their current trajectories, and
	 * expected friction.
	 * 
	 * @param otherItem
	 * @return true if collision imminent
	 */
	public abstract boolean willCollide(IMovingItem itemA, IMovingItem itemB, float deltaTime);

}