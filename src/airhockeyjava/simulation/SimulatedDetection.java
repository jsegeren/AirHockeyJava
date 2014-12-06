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

	/**
	 * Constructor. Instantiates the collision frame member.
	 * @param game
	 * @param inputLayer
	 */
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
}