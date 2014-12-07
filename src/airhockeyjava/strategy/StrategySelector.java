package airhockeyjava.strategy;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;

/**
 * Strategy selector. Transitions into the most appropriate strategy based on game conditions.
 * 
 * @author Joshua Segeren
 *
 */
public class StrategySelector {
	
	final private Game game;
	final private IStrategy triangleDefenseStrategy;
	final private IStrategy naiveDefenseStrategy;
	
	public StrategySelector(Game game) {
		this.game = game;
		this.triangleDefenseStrategy = new TriangleDefenseStrategy(game);
		this.naiveDefenseStrategy = new NaiveDefenseStrategy(game);
	}

	public IStrategy getBestStrategy() {
		if (game.gamePuck.getPosition().x < Constants.ROBOT_MALLET_INTIIAL_POSITION_X) {
			return naiveDefenseStrategy;
		}
		else {
			return triangleDefenseStrategy;
		}
	}

}