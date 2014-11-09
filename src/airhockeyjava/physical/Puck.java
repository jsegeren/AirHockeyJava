package airhockeyjava.physical;

import airhockeyjava.game.Constants;
import airhockeyjava.util.Vector2;

/**
 * Class to represent the puck.
 * 
 * @author Joshua Segeren
 *
 */
public class Puck extends MovingItem {

	/**
	 * Expected constructor.
	 * 
	 * @param position
	 * @param velocity
	 * @param color
	 */
	public Puck(Vector2 position, Vector2 velocity, float radius) {
		super(position, velocity, radius);
	}

	public Puck(Vector2 position, Vector2 velocity) {
		super(position, velocity, Constants.GAME_PUCK_RADIUS_METERS);
	}

	public Puck() {
		super(new Vector2(Constants.GAME_PUCK_INITIAL_POSITION_X,
				Constants.GAME_PUCK_INITIAL_POSITION_Y), new Vector2(
				Constants.GAME_PUCK_INITIAL_VELOCITY_X, Constants.GAME_PUCK_INITIAL_VELOCITY_Y),
				Constants.GAME_PUCK_RADIUS_METERS);
	}
}