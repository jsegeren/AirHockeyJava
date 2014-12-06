package airhockeyjava.control;

import airhockeyjava.util.Vector2;

/**
 * Interface for the path planning layer which is responsible for translating position-level 
 * control to velocity- or acceleration-level control. May be achieved with an internal state machine, or may be stateless.
 * 
 * @author Joshua Segeren
 *
 */
public interface IPathPlanner {

	/**
	 * Transforms the current target position to a target instantaneous velocity 
	 * @param targetPosition
	 * @return target velocity
	 */
	public Vector2 targetPositionToVelocity(Vector2 targetPosition);
	
	/**
	 * Transforms the current target position to a target instantaneous acceleration
	 * @param targetPosition
	 * @return target acceleration
	 */
	public Vector2 targetPositionToAcceleration(Vector2 targetPosition);
	
}