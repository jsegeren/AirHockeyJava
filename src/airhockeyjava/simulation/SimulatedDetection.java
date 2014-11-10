package airhockeyjava.simulation;

import airhockeyjava.game.Game;
import airhockeyjava.game.Constants;
import airhockeyjava.input.IInputLayer;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Mallet;
import airhockeyjava.physical.Puck;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Vector2;

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
		this.maxAllowedPuckX = game.gameTable.getWidth() - game.gamePuck.getRadius();
		this.minAllowedPuckY = game.gamePuck.getRadius();
		this.maxAllowedPuckY = game.gameTable.getHeight() - game.gamePuck.getRadius();
	}

	/* (non-Javadoc)
	 * @see airhockeyjava.simulation.IDetection#detectItemStates()
	 */
	@Override
	public void detectAndUpdateItemStates(float deltaTime) {
		updateItemStates(deltaTime);
	}

	@Override
	public boolean willCollide(IMovingItem itemA, IMovingItem itemB, float deltaT) {
		return true; // TODO actually implement this
	}

	/**
	 * Wrapper for updating states (position, velocity vectors) of individual game items.
	 * @param deltaTime
	 */
	private void updateItemStates(float deltaTime) {
		updateUserMalletState();
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

		// Now check for puck-mallet collisions over last-to-current interval.
		// TODO implement some energy loss into collision; right now assuming elastic and frictionless
		if (isColliding(game.gamePuck, game.userMallet)) {
			System.out.println(String.format("User hit the puck: %f %f",
					game.gamePuck.getPosition().x, game.gamePuck.getPosition().y));
			game.gamePuck.setVelocity(handleCollision(game.gamePuck, game.userMallet));
		}
		if (isColliding(game.gamePuck, game.robotMallet)) {
			System.out.println(String.format("User hit the puck: %f %f",
					game.gamePuck.getPosition().x, game.gamePuck.getPosition().y));
			game.gamePuck.setVelocity(handleCollision(game.gamePuck, game.robotMallet));
		}

		// Model surface friction loss
		// TODO Incorporate real physics!
		game.gamePuck.getVelocity().scl(1 - Constants.PUCK_SURFACE_FRICTION_LOSS_COEFFICIENT);
	}

	/**
	 * Internal method to check if two moving items are colliding
	 * @param itemA
	 * @param itemB
	 * @return
	 */
	private boolean isColliding(IMovingItem itemA, IMovingItem itemB) {
		return ((itemA != itemB) && new Vector2(itemA.getPosition()).sub(itemB.getPosition()).len() <= itemA
				.getRadius() + itemB.getRadius());
	}

	/**
	 * Internal method to handle collision between puck and mallet.
	 * TODO Account for mass differences between puck and mallet. Use energy formulation instead.
	 * @param puck
	 * @param mallet
	 * @return Vector2 the new velocity of the puck
	 */
	private Vector2 handleCollision(Puck puck, Mallet mallet) {
		// Correct position of puck to prevent cascading/duplicate collisions
		Vector2 unitVectorBetween = new Vector2(puck.getPosition()).sub(mallet.getPosition()).nor();
		puck.setPosition(new Vector2(mallet.getPosition()).add(unitVectorBetween.scl(puck
				.getRadius() + mallet.getRadius())));

		Vector2 malletToPuck = getTransferredForce(mallet, puck);
		Vector2 newPuckVelocity = new Vector2(malletToPuck).sub(puck.getVelocity());
		return newPuckVelocity.scl(1 - Constants.MALLET_PUCK_COLLISION_LOSS_COEFFICIENT).limit(
				Constants.MAX_PUCK_SPEED_METERS_PER_SECOND);

	}

	private Vector2 getTransferredForce(IMovingItem itemA, IMovingItem itemB) {
		Vector2 unitVectorBetween = new Vector2(itemA.getPosition()).sub(itemB.getPosition()).nor();
		Vector2 velocityA = new Vector2(itemA.getVelocity());

		if (!velocityA.equals(Vector2.Zero)) {
			// Consider angle between two items and velocity
			float collisionAngle = (float) Vector2.angleBetween(unitVectorBetween, velocityA);
			if (collisionAngle > 90f) {
				float impactOther = (collisionAngle - 90f) / 90f;
				float forceOther = velocityA.len() * impactOther;
				return unitVectorBetween.scl(forceOther).scl(-1f);
			}
		}
		return new Vector2();
	}

	/**
	 * Internal method to update user mallet based on mouse pointer.
	 * Note that we must explicitly prevent intersections as the mouse pointer can move instantaneously.
	 * This method is also responsible for calculating the velocity of the mallet.
	 * @param deltaTime
	 */
	private void updateUserMalletState() {
		//Get the mouse coordinates relative to the table
		int mouseX = inputLayer.getMouseX() - Constants.GUI_TABLE_OFFSET_X;
		int mouseY = inputLayer.getMouseY() - Constants.GUI_TABLE_OFFSET_Y;

		// Update the mallet position, restricting it to the bounds of the table
		// Must convert from the UI layer x-coordinate (raw pixel value) to the physical dimension
		float newPositionX = Math.max(
				((!game.settings.restrictUserMalletMovement) ? Conversion.pixelToMeter(mouseX)
						- game.userMallet.getRadius() : Math.min(Conversion.pixelToMeter(mouseX),
						Constants.GAME_TABLE_WIDTH_METERS / 2f - game.userMallet.getRadius())),
				game.userMallet.getRadius());
		float newPositionY = Math.max(
				Math.min(Conversion.pixelToMeter(mouseY), Constants.GAME_TABLE_HEIGHT_METERS
						- game.userMallet.getRadius()), game.userMallet.getRadius());

		Vector2 newPosition = new Vector2(newPositionX, newPositionY);
		game.userMallet.updatePositionAndCalculateVelocity(newPosition);
	}

	/**
	 * Move the robot paddle in response to the puck position and velocity
	 * TODO: Actually implement this properly
	 * @param deltaTime
	 */
	private void updateRobotMalletState(float deltaTime) {
		float newMallletPositionY = game.robotMallet.getPosition().y + (game.gamePuck.getPosition().y - game.robotMallet.getPosition().y) * 0.05f * deltaTime;

		game.robotMallet.updatePositionAndCalculateVelocity(
				new Vector2(
						game.robotMallet.getPosition().x,
						newMallletPositionY));
	}
}