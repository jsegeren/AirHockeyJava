package airhockeyjava.control;

import airhockeyjava.physical.Mallet;
import airhockeyjava.util.Vector2;

/**
 * User mallet controller, which simply controls the state of the mallet object directly.
 * No control signal outputs required.
 * 
 * @author Joshua Segeren
 *
 */
public class UserController implements IController {

	private final IPathPlanner pathPlanner;
	private final Mallet mallet;

	public UserController(Mallet mallet) {
		this.mallet = mallet;
		pathPlanner = new PathPlanner(mallet);
	}

	/**
	 * Set instantaneous mallet velocity and acceleration
	 */
	@Override
	public void controlMallet(Vector2 targetPosition, float deltaTime) {
		mallet.setAcceleration(pathPlanner.targetPositionToAcceleration(targetPosition));
		mallet.updatePositionAndVelocity(deltaTime);
	}

}