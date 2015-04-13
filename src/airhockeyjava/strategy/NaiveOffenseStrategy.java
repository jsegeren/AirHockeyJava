package airhockeyjava.strategy;

import java.awt.geom.Line2D;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

/**
 * Strategy layer. Naive implementation which considers all feasible combinations of moves, and chooses
 * move most likely to result in a goal.
 * NAIVE OFFENSE shoots through the puck in no particular direction
 * 
 * @author Joshua Segeren
 *
 */
public class NaiveOffenseStrategy implements IStrategy {

	final private static String strategyLabelString = Constants.STRATEGY_NAIVE_OFFENSE_STRING;
	final private Game game;

	public NaiveOffenseStrategy(Game game) {
		this.game = game;
	}

	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		// Position differential vector, scaled so that the "target" position is on the
		// opposite side of the puck
		
		// TODO improve this so puck doesn't get stuck in corners. Get on the correct side
		// of the puck.
		return new Vector2(game.gamePuck.getPosition()).add(
				new Vector2(game.gamePuck.getPosition()).sub(game.robotMallet.getPosition()).scl(
						0.1f));
	}

	@Override
	public String toString() {
		return strategyLabelString;
	}

	@Override
	public void initStrategy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Line2D[] getStrategyLines() {
		// TODO Auto-generated method stub
		return null;
	}

}