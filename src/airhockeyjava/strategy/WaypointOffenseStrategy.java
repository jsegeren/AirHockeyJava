package airhockeyjava.strategy;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import airhockeyjava.game.Constants;
import airhockeyjava.game.Game;
import airhockeyjava.util.Vector2;

/**
 * Strategy layer. Determines and sets waypoints, traverses the waypoints in sequence
 * to strike a puck
 * 
 * @author Joshua Segeren
 *
 */
public class WaypointOffenseStrategy implements IStrategy {

	final private static String strategyLabelString = Constants.STRATEGY_WAYPOINT_OFFENSE_STRING;
	final private Game game;

	private List<Vector2> waypointsList = new ArrayList<Vector2>();
	private int nextWaypointIndex = 0; // Index of the waypoint to which we are going

	public WaypointOffenseStrategy(Game game) {
		this.game = game;
	}

	@Override
	public Vector2 getTargetPosition(float deltaTime) {
		// TODO Update waypoints and index
		waypointsList.clear();
		Vector2 shotDirection = new Vector2(game.gameTable.gameTableUserGoalCenterPosition).sub(
				game.gamePuck.getPosition()).nor();

		waypointsList.add(new Vector2(game.gamePuck.getPosition()).add(new Vector2(shotDirection)
				.rotate(Constants.STRATEGY_VIA_ROTATE_ANGLE_DEGREES).scl(
						Constants.STRATEGY_VIA_DISTANCE_BEHIND_PUCK_METERS)));
		waypointsList.add(new Vector2(game.gamePuck.getPosition()).sub(new Vector2(shotDirection)
				.scl(Constants.STRATEGY_VIA_DISTANCE_BEHIND_PUCK_METERS)));
		waypointsList.add(new Vector2(game.gamePuck.getPosition()).add(new Vector2(shotDirection)
				.scl(Constants.STRATEGY_VIA_DISTANCE_AHEAD_PUCK_METERS)));

		// If we're behind puck first waypoint is behind puck
		// Otherwise (puck behind)/ we need to move perpendicular to avoid hitting puck in wrong direction
		if ((game.robotMallet.getPosition().x > game.gamePuck.getPosition().x)
				&& (nextWaypointIndex == 0)) {
			nextWaypointIndex++;
		}

		// Decide whether to move to next waypoint, by checking threshold distance
		// TODO optimize these experimentally
		if (waypointsList.get(nextWaypointIndex).dst2(game.robotMallet.getPosition()) <= 
				Constants.STRATEGY_VIA_SWITCH_DISTANCE_METERS_SQUARED) {
			if (nextWaypointIndex + 1 >= waypointsList.size()) {
				nextWaypointIndex = 0;
				return new Vector2(Constants.ROBOT_MALLET_INTIIAL_POSITION_X, Constants.ROBOT_MALLET_INITIAL_POSITION_Y);
			}
			nextWaypointIndex++;
		}
		return waypointsList.get(nextWaypointIndex);
	}

	@Override
	public String toString() {
		return strategyLabelString;
	}

	@Override
	public void initStrategy() {
		waypointsList.clear();
		nextWaypointIndex = 0;
	}
}