package airhockeyjava.strategy;

import java.awt.geom.Line2D;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Conversion;
import airhockeyjava.util.Vector2;

/**
 * Strategy selector. Transitions into the most appropriate strategy based on
 * game conditions.
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
	final private IStrategy homePositionStrategy;
	final private IStrategy aroundPuckStrategy;
	final private IStrategy retreatDefenseStrategy;

	private static IStrategy currentStrategy;

	private long lastUpdatedTime;

	public StrategySelector(Game game) {
		this.game = game;
		this.triangleDefenseStrategy = new TriangleDefenseStrategy(game);
		this.naiveDefenseStrategy = new NaiveDefenseStrategy(game);
		this.naiveOffenseStrategy = new NaiveOffenseStrategy(game);
		this.waypointOffenseStrategy = new WaypointOffenseStrategy(game);
		this.hybridDefenseStrategy = new HybridDefense(game);
		this.homePositionStrategy = new HomePositionStrategy(game);
		this.aroundPuckStrategy = new AroundPuckStrategy(game);
		this.retreatDefenseStrategy = new RetreatingDefenseStrategy(game);
		this.updateStrategy(naiveDefenseStrategy);
	}

	public IStrategy getBestStrategy() {
		Vector2 puckPosition = game.gamePuck.getPosition();
		Vector2 puckVelocity = game.gamePuck.getVelocity();

		boolean isHighSpeed = (puckVelocity.len() > Constants.STRATEGY_OFFENSE_MAX_PUCK_SPEED_TO_ENGAGE) ? true	: false;
		boolean isPuckInFrontOfMallet = (game.robotMallet.getPosition().x > puckPosition.x) ? true	: false;
		boolean isMalletInDefendableRegion = (game.robotMallet.getPosition().x > Constants.MALLET_DEFENDABLE_REGION) ? true	: false;
		boolean isPuckOnRobotSide = (game.gamePuck.getPosition().x > Constants.GAME_TABLE_WIDTH_METERS / 2) ? true : false;
		boolean shouldRetreat = false;
		
		System.out.println("STRATEGY: " + currentStrategy.getLabelString());
		System.out.println("isHighSpeed: " + isHighSpeed
				+ ", isPuckInFrontOfMallet: " + isPuckInFrontOfMallet
				+ ", isMalletInDefendableRegion: " + isMalletInDefendableRegion
				+ ", isPuckOnRobotSide: " + isPuckOnRobotSide
				+ ", shouldRetreat: " + shouldRetreat);

		// Retreat when the robot is vulnerable (not in a defendable region) and the user is able to shoot
		if (!isPuckOnRobotSide && !isMalletInDefendableRegion) {
			shouldRetreat = true;
		}

		if (shouldRetreat) {
			if (isMalletInDefendableRegion) {
				// Once we can safely defend stop retreating
				shouldRetreat = false;
			}

			updateStrategy(retreatDefenseStrategy);
		} else {
			if (!isHighSpeed && isPuckOnRobotSide) {
				// Puck is slow, ATTACK!
				if (isPuckInFrontOfMallet) {
					updateStrategy(waypointOffenseStrategy);
				} else {
					updateStrategy(aroundPuckStrategy);
				}
			} else {
				// Puck is fast, DEFEND!
				updateStrategy(hybridDefenseStrategy);

			}
		}

		// if (puckPosition.x > game.gameTable.getWidth() / 2) {
		// if(puckPosition.x > game.robotMallet.getPosition().x &&
		// puckPosition.x < (Constants.GAME_TABLE_WIDTH_METERS * 3)/4){
		// updateStrategy(aroundPuckStrategy);
		// }else
		// {
		//
		// if(puckVelocity.len() <
		// Constants.STRATEGY_OFFENSE_MAX_PUCK_SPEED_TO_ENGAGE){
		// updateStrategy(waypointOffenseStrategy);
		//
		// }else{
		// updateStrategy(hybridDefenseStrategy);
		//
		// }
		// }
		// } else {
		// if(puckVelocity.len() > 0.001f){
		// updateStrategy(hybridDefenseStrategy);
		// }else{
		// updateStrategy(homePositionStrategy);
		//
		// }
		// }
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

	public static Line2D[] getStrategyLines() {
		return currentStrategy.getStrategyLines();
	}
}