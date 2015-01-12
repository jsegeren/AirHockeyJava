package airhockeyjava.control;

import airhockeyjava.util.Vector2;

/**
 * Interface for the mechanical controller layer which is responsible for controlling 
 * the mallet, outputting appropriate control signals.
 * 
 * @author Joshua Segeren
 *
 */
public interface IController {
	
	/**
	 * Plan path and control mallet velocity and/or to reach target position.
	 * @param targetPosition
	 * @param deltaTime step interval
	 */
	public void controlMallet(Vector2 targetPosition, float deltaTime);
	
	/**
	 * Initialize the controller.
	 */
	public void initialize();

}