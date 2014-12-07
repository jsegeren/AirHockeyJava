package airhockeyjava.strategy;

import java.util.ArrayList;
import java.util.List;

import airhockeyjava.game.Constants;
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

	final private List<Vector2> availablePositions;

	final private Game game;

	public TriangleDefenseStrategy(Game game) {
		this.game = game;
		availablePositions = new ArrayList<Vector2>();
		availablePositions.add(new Vector2((float) game.gameTable.getWidth()
				- game.robotMallet.getRadius()
				- Constants.STRATEGY_TRIANGLE_DISTANCE_FROM_GOAL_METERS, (float) game.gameTable
				.getHeight() / 2f));
		availablePositions.add(new Vector2((float) game.gameTable.getWidth()
				- game.robotMallet.getRadius(), game.gameTable.getGoalStartY()));
		availablePositions.add(new Vector2((float) game.gameTable.getWidth()
				- game.robotMallet.getRadius(), game.gameTable.getGoalEndY()));
		availablePositions.add(new Vector2((float) game.gameTable.getWidth()
				- game.robotMallet.getRadius(), (float) game.gameTable.getHeight() / 2));

	}

	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		//		return new Vector2(game.robotMallet.getPosition().x, game.gamePuck.getPosition().y);
		float bestPositionScore = Float.MIN_VALUE;
		Vector2 bestPosition = null;
		for (Vector2 position : availablePositions) {
			float positionScore = getPositionScore(position);
			if (positionScore > bestPositionScore) {
				bestPositionScore = positionScore;
				bestPosition = position;
			}
		}
		return bestPosition;
	}

	/**
	 * Method to score each position. Higher score is better
	 * @param position
	 * @return score (higher is better)
	 */
	private float getPositionScore(Vector2 position) {
		// TODO revise the last expected point to give something more useful
		//		return 1/(game.gamePuck.getLastExpectedPointVector().dst2(position));
		//		return (float) (1 / (game.gamePuck.getPosition().dst2(position)));
		return (float) (1 / (game.gamePuck
				.getExpectedPosition(Constants.STRATEGY_TRIANGLE_LOOKAHEAD_TIME_SECONDS)
				.dst(position)));
	}
}