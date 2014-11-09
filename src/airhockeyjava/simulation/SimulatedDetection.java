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

	/* (non-Javadoc)
	 * @see airhockeyjava.simulation.IDetection#willCollide()
	 */
	@Override
	public boolean willCollide(IMovingItem itemA, IMovingItem itemB, float deltaT) {
		// TODO project out the positions of the items; if paths intersect at any point within deltaT return true
		// right now just returning whether they are ~ the same position
		return (Math.abs(itemA.getPosition().x - itemB.getPosition().x) <= Constants.INTERSECTION_EPSILON_METERS)
				&& (Math.abs(itemA.getPosition().y - itemB.getPosition().y) <= Constants.INTERSECTION_EPSILON_METERS);
	}

	private void updateItemStates(float deltaTime) {
		// First check for puck-wall collisions over last-to-current interval and reflect if necessary
		float newPuckPositionX = game.gamePuck.getPosition().x + game.gamePuck.getVelocity().x * deltaTime;
		if (newPuckPositionX >= game.gameTable.getWidth() || newPuckPositionX <= 0) {
			newPuckPositionX = game.gamePuck.getPosition().x - game.gamePuck.getVelocity().x * deltaTime;
			game.gamePuck.getVelocity().x = -1f * game.gamePuck.getVelocity().x;
		}
		float newPuckPositionY = game.gamePuck.getPosition().y + game.gamePuck.getVelocity().y * deltaTime;
		if (newPuckPositionY >= game.gameTable.getHeight() || newPuckPositionY <= 0) {
			game.gamePuck.getVelocity().y = -1f * game.gamePuck.getVelocity().y;
		}
		
		// Update positions
		game.gamePuck.getPosition().x = newPuckPositionX;
		game.gamePuck.getPosition().y = newPuckPositionY;
	}
}