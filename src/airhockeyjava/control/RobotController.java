package airhockeyjava.control;

import airhockeyjava.physical.Mallet;
import airhockeyjava.util.Vector2;

/**
 * Mechanical mallet controller, which is responsible for controlling 
 * the mallet, outputting appropriate control signals.
 * 
 * @author Joshua Segeren
 *
 */
public class RobotController implements IController {

	private final IPathPlanner pathPlanner;
	private final Mallet mallet;

	public RobotController(Mallet mallet) {
		this.mallet = mallet;
		pathPlanner = new PathPlanner(mallet);
	}

	/**
	 * Set instantaneous mallet velocity and acceleration
	 */
	@Override
	public void controlMallet(Vector2 targetPosition, float deltaTime) {
		//		mallet.setAcceleration(pathPlanner.targetPositionToAcceleration(targetPosition));
		mallet.setVelocity(pathPlanner.targetPositionToVelocity(targetPosition));

		// Update internal tracking variables for rendering
		mallet.updatePosition(deltaTime);
	}
}