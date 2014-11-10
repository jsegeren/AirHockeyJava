package airhockeyjava.simulation;

import airhockeyjava.game.Game;
import airhockeyjava.game.Constants;
import airhockeyjava.input.IInputLayer;
import airhockeyjava.physical.IMovingItem;
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

	public boolean isColliding(IMovingItem itemA, IMovingItem itemB) {
		// TODO project out the positions of the items; if paths intersect at any point within deltaT return true
		// right now just returning whether they are ~ the same position
		return (Math.abs(itemA.getPosition().x - itemB.getPosition().x) <= (itemA.getRadius() + itemB
				.getRadius()))
				&& (Math.abs(itemA.getPosition().y - itemB.getPosition().y) <= (itemA.getRadius() + itemB
						.getRadius()));
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
		updatePuckState(deltaTime);
		updateUserMalletState();
		if (game.settings.enableAI) updateRobotMalletState(deltaTime);
	}

	/**
	 * Internal method to update puck state. Note the assumptions currently in place for the physical model:
	 * 	1) Collisions are perfectly elastic. That is, momentum is preserved; however, we may want to incorporate
	 *   some energy loss, based on experimentation or a more sophisticated mechanical model.
	 *  2) Collision between puck and mallet do not affect the velocity/position of the mallet. That is, all
	 *   energy is transferred to the puck. This seems reasonable as the mallet is being held in a fixed position
	 *   but obviously this is not completely accurate.
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
			game.gamePuck.getVelocity().x = -1f * game.gamePuck.getVelocity().x;
		}
		float newPuckPositionY = game.gamePuck.getPosition().y + game.gamePuck.getVelocity().y
				* deltaTime;
		if (newPuckPositionY >= maxAllowedPuckY || newPuckPositionY <= minAllowedPuckY) {
			game.gamePuck.getVelocity().y = -1f * game.gamePuck.getVelocity().y;
		}

		// Now check for puck-mallet collisions over last-to-current interval.
		// TODO implement some energy loss into collision; right now assuming elastic and frictionless
		if (isColliding(game.gamePuck, game.userMallet)) {
			System.out.println(String.format("User hit the puck: %f %f",
					game.gamePuck.getPosition().x, game.gamePuck.getPosition().y));
			// TODO implement actual energy transfer based on velocity of items
			game.gamePuck.getVelocity().x = (game.gamePuck.getPosition().x > game.userMallet
					.getPosition().x) ? Constants.FAKE_VELOCITY_BURST : -1f
					* Constants.FAKE_VELOCITY_BURST;
			game.gamePuck.getVelocity().y = (game.gamePuck.getPosition().y > game.userMallet
					.getPosition().y) ? Constants.FAKE_VELOCITY_BURST : -1f
					* Constants.FAKE_VELOCITY_BURST;
		}
		if (isColliding(game.gamePuck, game.robotMallet)) {
			System.out.println(String.format("User hit the puck: %f %f",
					game.gamePuck.getPosition().x, game.gamePuck.getPosition().y));
			// TODO implement actual energy transfer based on velocity of items
			game.gamePuck.getVelocity().x = (game.gamePuck.getPosition().x > game.robotMallet
					.getPosition().x) ? Constants.FAKE_VELOCITY_BURST : -1f
					* Constants.FAKE_VELOCITY_BURST;
			game.gamePuck.getVelocity().y = (game.gamePuck.getPosition().y > game.robotMallet
					.getPosition().y) ? Constants.FAKE_VELOCITY_BURST : -1f
					* Constants.FAKE_VELOCITY_BURST;
		}

		// Update positions, enforcing boundary-based position constraints
		Vector2 newPuckPosition = new Vector2(Math.min(Math.max(newPuckPositionX, minAllowedPuckX),
				maxAllowedPuckX), Math.min(Math.max(newPuckPositionY, minAllowedPuckY),
				maxAllowedPuckY));
		game.gamePuck.setPosition(newPuckPosition);
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
				Math.min(
						Conversion.pixelToMeter(mouseX),
						(game.settings.restrictUserMalletMovement) ? Constants.GAME_TABLE_WIDTH_METERS / 2f : Constants.GAME_TABLE_WIDTH_METERS),
				0f);

		float newPositionY = Math.max(
				Math.min(Conversion.pixelToMeter(mouseY), Constants.GAME_TABLE_HEIGHT_METERS), 0f);

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