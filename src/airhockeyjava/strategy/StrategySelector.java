package airhockeyjava.strategy;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Vector2;

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
	final private IStrategy waypointOffenseStrategy;
	final private IStrategy hybridDefenseStrategy;

	private IStrategy currentStrategy;

	private long lastUpdatedTime;

	public StrategySelector(Game game) {
		this.game = game;
		this.triangleDefenseStrategy = new TriangleDefenseStrategy(game);
		this.naiveDefenseStrategy = new NaiveDefenseStrategy(game);
		this.naiveOffenseStrategy = new NaiveOffenseStrategy(game);
		this.waypointOffenseStrategy = new WaypointOffenseStrategy(game);
		this.hybridDefenseStrategy = new HybridDefence(game);

		this.updateStrategy(naiveDefenseStrategy);
	}

	public IStrategy getBestStrategy() {
//		Vector2 puckPosition = game.gamePuck.getPosition();
//		Vector2 puckVelocity = game.gamePuck.getVelocity();
//		if (puckPosition.x > game.gameTable.getWidth() / 2) {
//			if (puckPosition.x <= (float) game.gameTable.getWidth() - game.robotMallet.getRadius()
//					* 2 - Constants.STRATEGY_TRIANGLE_DISTANCE_FROM_GOAL_METERS) {
//				if (game.gamePuck.getVelocity().len() < Constants.STRATEGY_OFFENSE_MAX_PUCK_SPEED_TO_ENGAGE) {
//					updateStrategy(waypointOffenseStrategy);
//				} else {
//					updateStrategy(naiveDefenseStrategy);
//				}
//				// Check if puck moving slowly, and in the positive x direction
//			} else if (puckVelocity.len() < Constants.STRATEGY_OFFENSE_MAX_PUCK_SPEED_TO_ENGAGE
//					&& puckVelocity.x > 0) {
//				updateStrategy(waypointOffenseStrategy);
//			} else {
//				updateStrategy(triangleDefenseStrategy);
//			}
//		} else {
//			updateStrategy(triangleDefenseStrategy);
//		}
		updateStrategy(triangleDefenseStrategy);
		return currentStrategy;

	}

	private void updateStrategy(IStrategy desiredStrategy) {
		long currentTime = System.nanoTime();
		if (currentStrategy == null
				|| (!currentStrategy.equals(desiredStrategy) && (currentTime - lastUpdatedTime) > Conversion
						.secondsToNanoseconds(Constants.MIN_TIME_BETWEEN_STRATEGY_TRANSITION_SECONDS))) {
			System.out.println(desiredStrategy.toString());
			currentStrategy = desiredStrategy;
			// Reinitialize strategy objects with persisted state
			currentStrategy.initStrategy();
			lastUpdatedTime = currentTime;
		}
	}

}