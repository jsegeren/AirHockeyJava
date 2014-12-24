package airhockeyjava.physical;

import org.opencv.core.Scalar;

import airhockeyjava.detection.ITrackingObject;
import airhockeyjava.game.Constants;
import airhockeyjava.util.Vector2;

/**
 * Class to represent the puck.
 * 
 * @author Joshua Segeren
 * 
 */
public class Puck extends MovingItem implements ITrackingObject {
	// TODO set constants
	private static final Scalar HSVmin = Constants.DETECTION_PUCK_HSV_MIN;
	private static final Scalar HSVmax = Constants.DETECTION_PUCK_HSV_MAX;
	private static final int minObjectArea = Constants.DETECTION_PUCK_MIN_AREA;
	private static final int maxObjectArea = Constants.DETECTION_PUCK_MAX_AREA;

	/**
	 * Expected constructor.
	 * 
	 * @param position
	 * @param velocity
	 * @param color
	 */
	public Puck(Vector2 position, Vector2 velocity, float radius, Table table) {
		super(position, velocity, radius, Constants.GAME_PUCK_MASS_GRAMS, table);
	}

	public Puck(Vector2 position, Vector2 velocity, Table table) {
		super(position, velocity, Constants.GAME_PUCK_RADIUS_METERS,
				Constants.GAME_PUCK_MASS_GRAMS, table);
	}

	public Puck(Table table) {
		super(new Vector2(Constants.GAME_PUCK_INITIAL_POSITION_X,
				Constants.GAME_PUCK_INITIAL_POSITION_Y), new Vector2(
				Constants.GAME_PUCK_INITIAL_VELOCITY_X, Constants.GAME_PUCK_INITIAL_VELOCITY_Y),
				Constants.GAME_PUCK_RADIUS_METERS, Constants.GAME_PUCK_MASS_GRAMS, table);
	}

	@Override
	public Scalar getHSVMin() {
		return HSVmin;
	}

	@Override
	public Scalar getHSVMax() {
		return HSVmax;
	}

	@Override
	public int getMaxObjectArea() {
		return maxObjectArea;
	}

	@Override
	public int getMinObjectArea() {
		return minObjectArea;
	}
}