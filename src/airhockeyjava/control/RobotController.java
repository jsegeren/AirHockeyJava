package airhockeyjava.control;

import airhockeyjava.game.Constants;
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

	private final SerialConnection serialConnection;
	private final IPathPlanner pathPlanner;
	private final Mallet mallet;

	public RobotController(Mallet mallet, boolean isSerialRequired) {
		this.mallet = mallet;
		pathPlanner = new PathPlanner(mallet);
		serialConnection = isSerialRequired ? new SerialConnection() : null;
	}

	@Override
	/**
	 * Set up the serial connection of PC -> Arduino via USB
	 */
	public void initialize() {
		serialConnection.initialize();
	}

	/**
	 * Set instantaneous mallet velocity and acceleration
	 */
	@Override
	public void controlMallet(Vector2 targetPosition, float deltaTime) {
		// mallet.setAcceleration(pathPlanner.targetPositionToAcceleration(targetPosition));
		mallet.setVelocity(pathPlanner.targetPositionToVelocity(targetPosition));

		// Update internal tracking variables for rendering
		mallet.updatePosition(deltaTime);

		// Send/output control positions to Arduino
		if (serialConnection != null) {
			sendAbsolutePositionOverSerial(pathPlanner.targetPositionToSteps(targetPosition));
		}
	}

	private void sendAbsolutePositionOverSerial(Vector2 targetPositionAbsoluteSteps) {
		serialConnection.writeBytes(getDataStringFromPositionVector(targetPositionAbsoluteSteps)
				.getBytes());
	}

	private static String getDataStringFromPositionVector(Vector2 position) {
		return String.format("%s%s%s", position.x, Constants.SERIAL_POSITION_DELIMITER, position.y);
	}
}