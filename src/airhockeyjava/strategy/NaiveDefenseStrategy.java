package airhockeyjava.strategy;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

/**
 * Strategy layer. Naive implementation which considers all feasible combinations of moves, and chooses
 * move most likely to result in a goal.
 * NAIVE DEFENSE simply follows the side-to-side path of the puck.
 * 
 * @author Joshua Segeren
 *
 */
public class NaiveDefenseStrategy implements IStrategy {

	final private Game game;

	public NaiveDefenseStrategy(Game game) {
		this.game = game;
	}

	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		return new Vector2(Constants.ROBOT_MALLET_INTIIAL_POSITION_X, game.gamePuck.getPosition().y);
	}

}