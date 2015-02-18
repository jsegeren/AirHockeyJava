package airhockeyjava.control;

import airhockeyjava.game.Constants;
import airhockeyjava.physical.Mallet;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Vector2;

/**
 * Mechanical mallet controller, which is responsible for controlling 
 * the mallet, outputting appropriate control signals.
 * 
 * @author Joshua Segeren
 *
 */
public class RealRobotController implements IController {

	private final SerialConnection serialConnection;
	private final IPathPlanner pathPlanner;
	private final Mallet mallet;

	private class InvalidMessageException extends Exception {
		private static final long serialVersionUID = 8054257167367669050L;

		InvalidMessageException(String message) {
			super(message);
		}
	}

	public RealRobotController(Mallet mallet) {
		this.mallet = mallet;
		pathPlanner = new PathPlanner(mallet);
		serialConnection = new SerialConnection(this);
	}

	/**
	 * Set instantaneous mallet velocity and acceleration
	 */
	@Override
	public void controlMallet(Vector2 targetPosition, float deltaTime) {
		// Send/output control positions to Arduino
		if (serialConnection != null) {
			sendAbsolutePositionOverSerial(distancesToStepsVector(targetPosition));
		}
	}

	private void sendAbsolutePositionOverSerial(Vector2 targetPositionAbsoluteSteps) {
		serialConnection.writeBytes(getDataStringFromPositionVector(targetPositionAbsoluteSteps)
				.getBytes());
	}

	private static String getDataStringFromPositionVector(Vector2 position) {
		return String.format("%s%s%s", position.x, Constants.SERIAL_POSITION_DELIMITER, position.y);
	}

	@Override
	/**
	 * Set up the serial connection of PC -> Arduino via USB
	 */
	public void initialize() {
		serialConnection.initialize();
	}

	@Override
	public void handleInterfaceMessage(String interfaceMessage) {
		try {
			if (interfaceMessage.startsWith(Constants.SERIAL_POSITION_PREFIX)) {
				interfaceMessage = interfaceMessage.substring(Constants.SERIAL_POSITION_PREFIX
						.length());
				String[] stepPosition = interfaceMessage.split(Constants.SERIAL_POSITION_DELIMITER);
				if (stepPosition.length == 2) {
					mallet.setPosition(stepsToDistancesVector(new Vector2(Integer
							.parseInt(stepPosition[0]), Integer.parseInt(stepPosition[1]))));
				} else {
					throw new InvalidMessageException("Unexpected position message length.");
				}
			} else {
				throw new InvalidMessageException("Unexpected message prefix.");
			}
		} catch (Exception e) {
			System.out.println(String.format("Unable to process interface message %s: %s",
					interfaceMessage, e));
		}
	}

	private static Vector2 distancesToStepsVector(Vector2 position) {
		return new Vector2(Conversion.meterToStepsX(position.x),
				Conversion.meterToStepsY(position.y));
	}

	private static Vector2 stepsToDistancesVector(Vector2 steps) {
		return new Vector2(Conversion.stepsToMeterX((int) steps.x),
				Conversion.stepsToMeterY((int) steps.y));
	}
}