package airhockeyjava.strategy;

import java.awt.geom.Rectangle2D;

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
	
	private Game game;
	private final Rectangle2D tablePuckCollisionFrame;
	

	public NaiveDefenseStrategy(Game game) {
		this.game = game;
		this.tablePuckCollisionFrame = game.gameTable.getCollisionFrame(game.gamePuck.getRadius());
	}


	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		return new Vector2(game.robotMallet.getPosition().x, game.gamePuck.getPosition().y);
	}
	
}