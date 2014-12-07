package airhockeyjava.strategy;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Conversion;

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
	final private IStrategy naiveOffenseStrategy;

	private IStrategy currentStrategy;

	private long lastUpdatedTime;

	public StrategySelector(Game game) {
		this.game = game;
		this.triangleDefenseStrategy = new TriangleDefenseStrategy(game);
		this.naiveDefenseStrategy = new NaiveDefenseStrategy(game);
		this.naiveOffenseStrategy = new NaiveOffenseStrategy(game);
		this.updateStrategy(naiveDefenseStrategy);
	}

	public IStrategy getBestStrategy() {
		float puckPositionX = game.gamePuck.getPosition().x;
		if (puckPositionX > game.gameTable.getWidth() / 2) {
			if (puckPositionX <= (float) game.gameTable.getWidth() - game.robotMallet.getRadius()
					* 2 - Constants.STRATEGY_TRIANGLE_DISTANCE_FROM_GOAL_METERS) {
				if (game.gamePuck.getVelocity().len() < Constants.STRATEGY_OFFENSE_MAX_PUCK_SPEED_TO_ENGAGE) {
					updateStrategy(naiveOffenseStrategy);
				} else {
					updateStrategy(naiveDefenseStrategy);
				}
			} else if (game.gamePuck.getVelocity().len() < Constants.STRATEGY_OFFENSE_MAX_PUCK_SPEED_TO_ENGAGE) {
				updateStrategy(naiveOffenseStrategy);
			} else {
				updateStrategy(triangleDefenseStrategy);
			}
		} else {
			updateStrategy(triangleDefenseStrategy);
		}
		return currentStrategy;

	}

	private void updateStrategy(IStrategy desiredStrategy) {
		long currentTime = System.nanoTime();
		if (currentStrategy == null
				|| (!currentStrategy.equals(desiredStrategy) && (currentTime - lastUpdatedTime) > Conversion
						.secondsToNanoseconds(Constants.MIN_TIME_BETWEEN_STRATEGY_TRANSITION_SECONDS))) {
			System.out.println(desiredStrategy.toString());
			currentStrategy = desiredStrategy;
			lastUpdatedTime = currentTime;
		}
	}

}