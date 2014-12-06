package airhockeyjava.physical;

import java.awt.geom.Line2D;

import airhockeyjava.util.Vector2;

/**
 * Interface for all moving pieces on table.
 * 
 * @author Joshua Segeren
 *
 */
public interface IMovingItem {

	/**
	 * Method to retrieve current position trajectory of item over current interval
	 * @return Line2D line segment of trajectory
	 */
	public abstract Line2D getTrajectoryLine();

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
	 * Method to retrieve the acceleration of the item (if available) for the current state.
	 * 
	 * @return Vector2 the acceleration
	 */
	public abstract Vector2 getAcceleration();

	/**
	 * Explicit setter for acceleration
	 * 
	 * @param newAcceleration
	 */
	public abstract void setAcceleration(Vector2 newAcceleration);

	/**
	 * Method to update position, velocity based on previous (known) state information and acceleration.
	 * @param deltaTime time elapsed
	 */
	public abstract void updatePositionAndVelocity(float deltaTime);

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
