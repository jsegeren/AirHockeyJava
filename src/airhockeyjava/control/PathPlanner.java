package airhockeyjava.control;

import airhockeyjava.game.Constants;
import airhockeyjava.physical.Mallet;
import airhockeyjava.util.Vector2;

/**
 * Path planning layer; responsible for mapping path from current mallet position to target position.
 * Naive implementation uses instantaneous acceleration calculation, then passes velocity to motor controller.
 * 
 * @author Joshua Segeren
 *
 */
public class PathPlanner implements IPathPlanner {

	public final Mallet mallet;

	protected PathPlanner(Mallet mallet) {
		this.mallet = mallet;
	}

	/**
	 * Maximizes velocity in intended direction of movement
	 */
	@Override
	public Vector2 targetPositionToVelocity(Vector2 targetPosition) {
		Vector2 positionDeltaVector = getPositionDeltaVector(targetPosition);
		if (positionDeltaVector.len() < Constants.MECHANICAL_MAX_POSITION_RESOLUTION_METERS) {
			return new Vector2();
		}
		else {
			return positionDeltaVector.nor().scl(Constants.MECHANICAL_MAX_SPEED_METERS_PER_SECOND);
		}
	}

	@Override
	// Gets the appropriate current smoothing acceleration (limiting overshoot) based on position difference.
	public Vector2 targetPositionToAcceleration(Vector2 targetPosition) {
		return getPositionDeltaVector(targetPosition).scl(Constants.DIRECTIONAL_FORCE_SCALE_FACTOR)
				.sub(new Vector2(mallet.getVelocity()).scl(Constants.DAMPENING_FORCE_SCALE_FACTOR));
	}

	private Vector2 getPositionDeltaVector(Vector2 targetPosition) {
		return new Vector2(targetPosition).sub(mallet.getPosition());
	}
	
	@Override
	public Vector2 targetPositionToSteps(Vector2 targetPosition) {
		// TODO implement conversion
		return targetPosition;
	}
}