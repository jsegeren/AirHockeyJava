package airhockeyjava.simulation;

import java.awt.geom.Rectangle2D;

import airhockeyjava.game.Game;
import airhockeyjava.game.Constants;
import airhockeyjava.input.IInputLayer;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Vector2;
import airhockeyjava.simulation.Collision;
import airhockeyjava.simulation.PuckSimulation;

/**
 * Class for the simulated game physics. Mocks out the detection layer for simulating game physics.
 * TODO determine whether detection layer is responsible for choosing which it should update or if
 * this should be specified by the constructing/calling class (i.e. Game class).
 *
 * @author Joshua Segeren
 *
 */
public class SimulatedDetection implements IDetection {
	private Game game;
	private IInputLayer inputLayer;

	private final Rectangle2D tablePuckCollisionFrame;

	public SimulatedDetection(Game game, IInputLayer inputLayer) {
		this.game = game;
		this.inputLayer = inputLayer;
		this.tablePuckCollisionFrame = game.gameTable.getCollisionFrame(game.gamePuck.getRadius());
	}

	/* (non-Javadoc)
	 * @see airhockeyjava.simulation.IDetection#detectItemStates()
	 */
	@Override
	public void detectAndUpdateItemStates(float deltaTime) {
		updateItemStates(deltaTime);
	}

	/**
	 * Wrapper for updating states (position, velocity vectors) of individual game items.
	 * @param deltaTime
	 */
	private void updateItemStates(float deltaTime) {
		// Check for and update goals scored
		if (game.settings.goalDetectionOn) {
			PuckSimulation.GoalScoredEnum goalScoredEnum = PuckSimulation.checkAndUpdateGoalScored(
					game.gamePuck, game.gameTable, deltaTime);
			if (goalScoredEnum.equals(PuckSimulation.GoalScoredEnum.GOAL_SCORED_FOR_ROBOT)) {
				game.robotScore++;
				game.resetPuck();
				return;
			} else if (goalScoredEnum.equals(PuckSimulation.GoalScoredEnum.GOAL_SCORED_FOR_USER)) {
				game.userScore++;
				game.resetPuck();
				return;
			}
		}

		// Update position, velocity, and projection if necessary
		updatePuckState(deltaTime);

		// Update robot mallet via simulated AI 
		// TODO move this call to top level game logic. Simulated detection layer should only worry about
		// the puck state.
		if (game.settings.enableAI) {
			updateRobotMalletState(deltaTime);
		}

		// Update user-controlled mallet
		updateUserMalletState(deltaTime);
	}

	/**
	 * Internal method to update puck state!
	 * @param deltaTime
	 */
	private void updatePuckState(float deltaTime) {
		boolean isPuckCollision = false; // Flag to check whether puck collision occurred
		// Update puck position, velocity based on wall collisions
		isPuckCollision |= PuckSimulation.updatePuckFromWallCollisions(game.gamePuck,
				tablePuckCollisionFrame, deltaTime);
		// Update puck position, velocity based on mallet collision
		isPuckCollision |= PuckSimulation.updatePuckFromMalletCollisions(game.gamePuck,
				game.userMallet, game.robotMallet, deltaTime);

		// Apply air friction. Surface is assumed frictionless // TODO is this reasonable?
		PuckSimulation.applyAirFrictionToPuckVelocity(game.gamePuck,
				Constants.PUCK_AIR_FRICTION_COEFFICIENT, deltaTime);

		// Cap out the maximum puck speed
		game.gamePuck.getVelocity().limit(Constants.MAX_PUCK_SPEED_METERS_PER_SECOND);

		// Update predicted path
		game.gamePuck.updatePredictedPath(
				game.gameTable.getCollisionFrame(game.gamePuck.getRadius()),
				Constants.NUMBER_PREDICTED_PATH_REFLECTIONS, isPuckCollision);
	}

	/**
	 * Internal method to update user mallet based on mouse pointer.
	 * Note that we must explicitly prevent intersections as the mouse pointer can move instantaneously.
	 * This method is also responsible for calculating the velocity of the mallet.
	 * @param deltaTime
	 */
	private void updateUserMalletState(float deltaTime) {
		// Get the mouse coordinates relative to the table
		int mouseX = inputLayer.getMouseX() - Constants.GUI_TABLE_OFFSET_X;
		int mouseY = inputLayer.getMouseY() - Constants.GUI_TABLE_OFFSET_Y;

		// Update the mallet position, restricting it to the bounds of the table
		// Must convert from the UI layer x-coordinate (raw pixel value) to the physical dimension
		float targetPositionX = (float) Math.max(
				((!game.settings.restrictUserMalletMovement) ? Conversion.pixelToMeter(mouseX)
						- game.userMallet.getRadius() : Math.min(Conversion.pixelToMeter(mouseX),
						game.gameTable.getWidth() / 2f - game.userMallet.getRadius())),
				game.userMallet.getRadius());

		float targetPositionY = (float) Math.max(
				Math.min(Conversion.pixelToMeter(mouseY), game.gameTable.getHeight()
						- game.userMallet.getRadius()), game.userMallet.getRadius());

		float diffX = targetPositionX - game.userMallet.getPosition().x;
		float diffY = targetPositionY - game.userMallet.getPosition().y;

		game.userMallet.setAcceleration(getRequiredAcceleration(diffX, diffY,
				game.userMallet.getVelocity()));
		game.userMallet.updatePositionAndVelocity(deltaTime);
	}

	/**
	 * Move the robot paddle in response to the puck position and velocity
	 * TODO: Actually implement this properly
	 * @param deltaTime
	 */
	private void updateRobotMalletState(float deltaTime) {

		float diffX = 0;
		float diffY = game.gamePuck.getPosition().y - game.robotMallet.getPosition().y;

		game.robotMallet.setAcceleration(getRequiredAcceleration(diffX, diffY,
				game.robotMallet.getVelocity()));
		game.robotMallet.updatePositionAndVelocity(deltaTime);

	}

	/**
	 * Gets the appropriate current smoothing acceleration (limiting overshoot) based on position difference.
	 * @param diffX
	 * @param diffY
	 * @param velocity
	 * @return acceleration vector
	 */
	private static Vector2 getRequiredAcceleration(float diffX, float diffY, Vector2 velocity) {
		return new Vector2(diffX, diffY).scl(Constants.DIRECTIONAL_FORCE_SCALE_FACTOR).sub(
				new Vector2(velocity).scl(Constants.DAMPENING_FORCE_SCALE_FACTOR));

	}
}