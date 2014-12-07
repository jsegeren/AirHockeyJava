package airhockeyjava.strategy;

import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

/**
 * Strategy layer. Naive triangle defense, which sets up a triangle in front of the goal to
 * deflect all possible incoming shots (straight, under, over).
 * 
 * @author Joshua Segeren
 *
 */
public class TriangleDefenseStrategy implements IStrategy {
	
	final private Game game;
	

	public TriangleDefenseStrategy(Game game) {
		this.game = game;
	}


	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		return new Vector2(game.robotMallet.getPosition().x, game.gamePuck.getPosition().y);
	}
	
}