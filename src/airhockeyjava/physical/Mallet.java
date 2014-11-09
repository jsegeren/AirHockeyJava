package airhockeyjava.physical;

import airhockeyjava.game.Constants;
import airhockeyjava.util.Vector2;

/**
 * Class to represent the mallet.
 * 
 * @author Joshua Segeren
 *
 */
public class Mallet extends MovingItem {

	public boolean isUserControlled; // Is the mallet controlled by the user (or is is robot-controlled)?

	/**
	 * Expected constructor.
	 * 
	 * @param position
	 * @param velocity
	 * @param color
	 */
	public Mallet(Vector2 position, Vector2 velocity, float radius, boolean isUserControlled) {
		super(position, velocity, radius);
		this.isUserControlled = isUserControlled;
	}

	/**
	 * Overloaded.
	 * 
	 * @param position
	 * @param velocity
	 * @param isUserControlled
	 */
	public Mallet(Vector2 position, Vector2 velocity, boolean isUserControlled) {
		super(position, velocity, Constants.GAME_MALLET_RADIUS_METERS);
		this.isUserControlled = isUserControlled;
	}

	/**
	 * Overloaded.
	 * 
	 * @param isUserControlled
	 */
	public Mallet(boolean isUserControlled) {
		super(new Vector2(isUserControlled ? Constants.USER_MALLET_INITIAL_POSITION_X
				: Constants.ROBOT_MALLET_INTIIAL_POSITION_X,
				isUserControlled ? Constants.USER_MALLET_INITIAL_POSITION_Y
						: Constants.ROBOT_MALLET_INITIAL_POSITION_Y), new Vector2(
				isUserControlled ? Constants.USER_MALLET_INITIAL_VELOCITY_X
						: Constants.ROBOT_MALLET_INITIAL_VELOCITY_X,
				isUserControlled ? Constants.USER_MALLET_INITIAL_VELOCITY_Y
						: Constants.ROBOT_MALLET_INITIAL_VELOCITY_Y),
				Constants.GAME_MALLET_RADIUS_METERS);
		this.isUserControlled = isUserControlled;
	}
}