package airhockeyjava.simulation;

import airhockeyjava.game.Game;
import airhockeyjava.game.Constants;
import airhockeyjava.physical.IMovingItem;

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

	public SimulatedDetection(Game game) {
		this.game = game;
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
		return (Math.abs(itemA.getPosition().x - itemB.getPosition().x) <= Constants.INTERSECTION_EPSILON_METERS)
				&& (Math.abs(itemA.getPosition().y - itemB.getPosition().y) <= Constants.INTERSECTION_EPSILON_METERS);
	}

	@Override
	public boolean willCollide(IMovingItem itemA, IMovingItem itemB, float deltaT) {
		return true; // TODO actually implement this
	}

	private void updateItemStates(float deltaTime) {
		// First check for puck-wall collisions over last-to-current interval and reflect if necessary
		// TODO implement some energy loss into collision; right now assuming elastic and frictionless
		float newPuckPositionX = game.gamePuck.getPosition().x + game.gamePuck.getVelocity().x
				* deltaTime;
		if (newPuckPositionX >= game.gameTable.getWidth() || newPuckPositionX <= 0) {
			newPuckPositionX = game.gamePuck.getPosition().x - game.gamePuck.getVelocity().x
					* deltaTime;
			game.gamePuck.getVelocity().x = -1f * game.gamePuck.getVelocity().x;
		}
		float newPuckPositionY = game.gamePuck.getPosition().y + game.gamePuck.getVelocity().y
				* deltaTime;
		if (newPuckPositionY >= game.gameTable.getHeight() || newPuckPositionY <= 0) {
			game.gamePuck.getVelocity().y = -1f * game.gamePuck.getVelocity().y;
		}

		// Now check for puck-mallet collisions over last-to-current interval.
		// TODO implement some energy loss into collision; right now assuming elastic and frictionless
		if (isColliding(game.gamePuck, game.userMallet)) {
			System.out.println(String.format("User hit the puck: %d %d",
					game.gamePuck.getPosition().x, game.gamePuck.getPosition().y));
			// TODO implement actual energy transfer based on velocity of items
			game.gamePuck.getVelocity().x = (game.gamePuck.getPosition().x > game.userMallet
					.getPosition().x) ? 1 : -1;
			game.gamePuck.getVelocity().y = (game.gamePuck.getPosition().y > game.userMallet
					.getPosition().y) ? 1 : -1;
		}
		if (isColliding(game.gamePuck, game.robotMallet)) {
			System.out.println(String.format("User hit the puck: %d %d",
					game.gamePuck.getPosition().x, game.gamePuck.getPosition().y));
			// TODO implement actual energy transfer based on velocity of items
			game.gamePuck.getVelocity().x = (game.gamePuck.getPosition().x > game.userMallet
					.getPosition().x) ? 1 : -1;
			game.gamePuck.getVelocity().y = (game.gamePuck.getPosition().y > game.userMallet
					.getPosition().y) ? 1 : -1;
		}

		// Update positions
		game.gamePuck.getPosition().x = newPuckPositionX;
		game.gamePuck.getPosition().y = newPuckPositionY;
	}
}