package airhockeyjava.simulation;

import airhockeyjava.game.Game;
import airhockeyjava.game.Constants;
import airhockeyjava.input.IInputLayer;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Vector2;
import airhockeyjava.simulation.Collision;

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

	// TODO could make these constants; right now we initialize at construction time with final values
	private final float minAllowedPuckX;
	private final float maxAllowedPuckX;
	private final float minAllowedPuckY;
	private final float maxAllowedPuckY;

	public SimulatedDetection(Game game, IInputLayer inputLayer) {
		this.game = game;
		this.inputLayer = inputLayer;
		this.minAllowedPuckX = game.gamePuck.getRadius();
		this.maxAllowedPuckX = (float) (game.gameTable.getWidth() - game.gamePuck.getRadius());
		this.minAllowedPuckY = game.gamePuck.getRadius();
		this.maxAllowedPuckY = (float) (game.gameTable.getHeight() - game.gamePuck.getRadius());
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
		updateUserMalletState(deltaTime);
		updatePuckState(deltaTime);
		if (game.settings.enableAI) {
			updateRobotMalletState(deltaTime);
		}
	}

	/**
	 * Internal method to update puck state. Note the assumptions currently in place for the physical model:
	 * 	1) Collisions are perfectly elastic. That is, momentum is preserved; however, we may want to incorporate
	 *   some energy loss, based on experimentation or a more sophisticated mechanical model.
	 *  2) Collision between puck and mallet do not affect the velocity/position of the mallet.
	 * 
	 * Reference (billiard collisions i.e. 1st year physics):
	 * http://www.real-world-physics-problems.com/physics-of-billiards.html 
	 * 
	 * @param deltaTime
	 */
	private void updatePuckState(float deltaTime) {
		// First check for puck-wall collisions over last-to-current interval and reflect if necessary
		// TODO implement some energy loss into collision; right now assuming elastic and frictionless
		float newPuckPositionX = game.gamePuck.getPosition().x + game.gamePuck.getVelocity().x
				* deltaTime;
		if (newPuckPositionX >= maxAllowedPuckX || newPuckPositionX <= minAllowedPuckX) {
			newPuckPositionX = game.gamePuck.getPosition().x - game.gamePuck.getVelocity().x
					* deltaTime;
			game.gamePuck.getVelocity().x *= -1f + Constants.WALL_PUCK_COLLISION_LOSS_COEFFICIENT;
		}
		float newPuckPositionY = game.gamePuck.getPosition().y + game.gamePuck.getVelocity().y
				* deltaTime;
		if (newPuckPositionY >= maxAllowedPuckY || newPuckPositionY <= minAllowedPuckY) {
			game.gamePuck.getVelocity().y *= -1f + Constants.WALL_PUCK_COLLISION_LOSS_COEFFICIENT;
		}
		// Update puck position, enforcing boundary-based position constraints
		Vector2 newPuckPosition = new Vector2(Math.min(Math.max(newPuckPositionX, minAllowedPuckX),
				maxAllowedPuckX), Math.min(Math.max(newPuckPositionY, minAllowedPuckY),
				maxAllowedPuckY));

		game.gamePuck.setPosition(newPuckPosition);
		game.gamePuck.updateTrajectory();
		game.gamePuck.updatePredictedPath(game.gameTable.getCollisionFrame(game.gamePuck
				.getRadius()));

		// Check if puck-mallet collision HAS occurred
		if (Collision.hasCollided(game.gamePuck, game.userMallet)) {
			System.out.println("HAS COLLIDED!");
		}
		if (Collision.hasCollided(game.gamePuck, game.robotMallet)) {
			System.out.println("Robot HAS COLLIDED!");
		}

		// Now check for puck-mallet collisions over last-to-current interval.
		// TODO implement some energy loss into collision; right now assuming elastic and frictionless
		if (Collision.isColliding(game.gamePuck, game.userMallet)
				|| Collision.hasCollided(game.gamePuck, game.userMallet)) {
			//			System.out.println(String.format("User mallet velocity before: (%f, %f)",
			//					game.userMallet.getVelocity().x, game.userMallet.getVelocity().y));
			//			System.out.println(String.format("Puck speed before: (%f, %f)",
			//					game.gamePuck.getVelocity().x, game.gamePuck.getVelocity().y));
			game.gamePuck.setVelocity(Collision.handleCollision(game.gamePuck, game.userMallet)
					.scl(1 - Constants.MALLET_PUCK_COLLISION_LOSS_COEFFICIENT));
		}
		if (Collision.isColliding(game.gamePuck, game.robotMallet)
				|| Collision.hasCollided(game.gamePuck, game.robotMallet)) {
			//			System.out.println(String.format("Puck speed before: (%f, %f)",
			//					game.gamePuck.getVelocity().x, game.gamePuck.getVelocity().y));
			game.gamePuck.setVelocity(Collision.handleCollision(game.gamePuck, game.robotMallet)
					.scl(1 - Constants.MALLET_PUCK_COLLISION_LOSS_COEFFICIENT));
		}

		// Model surface friction loss
		// TODO Incorporate real physics!
		game.gamePuck.getVelocity().scl(1 - Constants.PUCK_SURFACE_FRICTION_LOSS_COEFFICIENT)
				.limit(Constants.MAX_PUCK_SPEED_METERS_PER_SECOND);
	}

	/**
	 * Internal method to update user mallet based on mouse pointer.
	 * Note that we must explicitly prevent intersections as the mouse pointer can move instantaneously.
	 * This method is also responsible for calculating the velocity of the mallet.
	 * @param deltaTime
	 */
	private void updateUserMalletState(float deltaTime) {
		//Get the mouse coordinates relative to the table
		int mouseX = inputLayer.getMouseX() - Constants.GUI_TABLE_OFFSET_X;
		int mouseY = inputLayer.getMouseY() - Constants.GUI_TABLE_OFFSET_Y;

		// Update the mallet position, restricting it to the bounds of the table
		// Must convert from the UI layer x-coordinate (raw pixel value) to the physical dimension
		float newPositionX = (float) Math.max(
				((!game.settings.restrictUserMalletMovement) ? Conversion.pixelToMeter(mouseX)
						- game.userMallet.getRadius() : Math.min(Conversion.pixelToMeter(mouseX),
						game.gameTable.getWidth() / 2f - game.userMallet.getRadius())),
				game.userMallet.getRadius());
		float newPositionY = (float) Math.max(
				Math.min(Conversion.pixelToMeter(mouseY), game.gameTable.getHeight()
						- game.userMallet.getRadius()), game.userMallet.getRadius());

		Vector2 newPosition = new Vector2(newPositionX, newPositionY);
		game.userMallet.updatePositionAndCalculateVelocity(newPosition, deltaTime);
	}

	/**
	 * Move the robot paddle in response to the puck position and velocity
	 * TODO: Actually implement this properly
	 * @param deltaTime
	 */
	private void updateRobotMalletState(float deltaTime) {
		float newMalletPositionY = game.robotMallet.getPosition().y
				+ (game.gamePuck.getPosition().y - game.robotMallet.getPosition().y) * 0.05f
				* deltaTime;

		game.robotMallet.updatePositionAndCalculateVelocity(
				new Vector2(game.robotMallet.getPosition().x, newMalletPositionY), deltaTime);
	}
}