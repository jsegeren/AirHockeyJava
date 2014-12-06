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

	@Override
	public Vector2 targetPositionToVelocity(Vector2 targetPosition) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	// Gets the appropriate current smoothing acceleration (limiting overshoot) based on position difference.
	public Vector2 targetPositionToAcceleration(Vector2 targetPosition) {
		Vector2 positionDelta = new Vector2(targetPosition).sub(mallet.getPosition());
		return positionDelta.scl(Constants.DIRECTIONAL_FORCE_SCALE_FACTOR).sub(
				new Vector2(mallet.getVelocity()).scl(Constants.DAMPENING_FORCE_SCALE_FACTOR));
	}
}