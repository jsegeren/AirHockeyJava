package airhockeyjava.simulation;

import java.awt.geom.Rectangle2D;

import airhockeyjava.game.Constants;
import airhockeyjava.physical.Mallet;
import airhockeyjava.physical.Puck;
import airhockeyjava.physical.Table;
import airhockeyjava.util.Vector2;

/**
 * Class which defines simulated puck behavior. Used by both the simulated detection/physics engine, and 
 * by the strategy engine for predicted where puck will be, in order to decide how to respond.
 * @author Joshua Segeren
 *
 */
public class PuckSimulation {
	public enum GoalScoredEnum {
		GOAL_SCORED_FOR_USER, GOAL_SCORED_FOR_ROBOT, NO_GOAL_SCORED
	}

	public static GoalScoredEnum checkAndUpdateGoalScored(Puck puck, Table table, float deltaTime) {
		// First check for puck-wall collisions over last-to-current interval and reflect if necessary
		// TODO implement some energy loss into collision; right now assuming elastic and frictionless
		float newPuckPositionX = puck.getPosition().x + puck.getVelocity().x * deltaTime;
		float newPuckPositionY = puck.getPosition().y + puck.getVelocity().y * deltaTime;

		// Check if goal scored and return back appropriate enum
		Rectangle2D tablePuckCollisionFrame = table.getCollisionFrame(puck.getRadius());
		if (puck.isPointIntersectingLeftGoal(newPuckPositionX, newPuckPositionY,
				table.getCollisionFrame(puck.getRadius()), table.getGoalWidth())) {
			return GoalScoredEnum.GOAL_SCORED_FOR_ROBOT;

		} else if (puck.isPointIntersectingRightGoal(newPuckPositionX, newPuckPositionY,
				tablePuckCollisionFrame, table.getGoalWidth())) {
			return GoalScoredEnum.GOAL_SCORED_FOR_USER;
		}
		return GoalScoredEnum.NO_GOAL_SCORED;
	}

	/**
	 * Static method to update puck state. Note the assumptions currently in place for the physical model:
	 * 	1) Collisions are perfectly elastic. That is, momentum is preserved; however, we may want to incorporate
	 *   some energy loss, based on experimentation or a more sophisticated mechanical model.
	 *  2) Collision between puck and mallet do not affect the velocity/position of the mallet.
	 *
	 * Reference (billiard collisions i.e. 1st year physics):
	 * http://www.real-world-physics-problems.com/physics-of-billiards.html
	 *
	 * Note that this method DOES modify the passed puck object velocity and position based on wall reflections.
	 * @param puck
	 * @param tablePuckCollisionFrame
	 * @param deltaTime
	 * @return true iff collision handled
	 * 
	 */
	public static boolean updatePuckFromWallCollisions(Puck puck, Rectangle2D tablePuckCollisionFrame,
			float deltaTime) {
		boolean isCollision = false;
		// First check for puck-wall collisions over last-to-current interval and reflect if necessary
		// TODO implement some energy loss into collision; right now assuming elastic and frictionless
		float newPuckPositionX = puck.getPosition().x + puck.getVelocity().x * deltaTime;
		float newPuckPositionY = puck.getPosition().y + puck.getVelocity().y * deltaTime;

		// Check for puck-wall collisions over last-to-current interval and modify (reflect) velocities
		// accordingly. TODO implement some energy loss into collision; right now assuming elastic and frictionless
		if (newPuckPositionX >= tablePuckCollisionFrame.getMaxX()
				|| newPuckPositionX <= tablePuckCollisionFrame.getMinX()) {
			isCollision = true;
			puck.getVelocity().x *= -1f;
			// Apply restitution (i.e. not perfectly elastic collision)
			applyRestitutionToPuckVelocity(puck, Constants.WALL_PUCK_COLLISION_RESTITUTION_COEFFICIENT);
			// Revise position projections
			newPuckPositionX = puck.getPosition().x + puck.getVelocity().x * deltaTime;
		}
		if (newPuckPositionY >= tablePuckCollisionFrame.getMaxY()
				|| newPuckPositionY <= tablePuckCollisionFrame.getMinY()) {
			isCollision = true;
			puck.getVelocity().y *= -1f;
			// Apply restitution (i.e. not perfectly elastic collision)
			applyRestitutionToPuckVelocity(puck, Constants.WALL_PUCK_COLLISION_RESTITUTION_COEFFICIENT);
			// Revise position projections
			newPuckPositionY = puck.getPosition().y + puck.getVelocity().y * deltaTime;
		}

		// Return puck position, enforcing boundary-based position constraints
		puck.setPosition(new Vector2((float) Math.min(
				Math.max(newPuckPositionX, tablePuckCollisionFrame.getMinX()),
				tablePuckCollisionFrame.getMaxX()), (float) Math.min(
				Math.max(newPuckPositionY, tablePuckCollisionFrame.getMinY()),
				tablePuckCollisionFrame.getMaxY())));
		
		return isCollision;
	}

	/**
	 * Static method to update puck position, velocity based on mallet collisions over
	 * elapsed time interval.
	 * @param puck
	 * @param malletA
	 * @param malletB
	 * @param deltaTime
	 * @return true iff collision handled
	 */
	public static boolean updatePuckFromMalletCollisions(Puck puck, Mallet malletA, Mallet malletB,
			float deltaTime) {
		boolean isCollision = false;
		if (Collision.isColliding(puck, malletA) || Collision.hasCollided(puck, malletA)) {
			isCollision = true;
			puck.setVelocity(Collision.handleCollision(puck, malletA).scl(
					Constants.MALLET_PUCK_COLLISION_RESTITUTION_COEFFICIENT));
		}
		if (Collision.isColliding(puck, malletB) || Collision.hasCollided(puck, malletB)) {
			isCollision = true;
			puck.setVelocity(Collision.handleCollision(puck, malletB).scl(
					Constants.MALLET_PUCK_COLLISION_RESTITUTION_COEFFICIENT));
		}
		return isCollision;
	}

	/**
	 * Attenuate puck velocity by applying air friction
	 * Where v_t = v_0 * e ^ (-kt/m)
	 * @param puck
	 * @param coefficientOfAirFriction
	 */
	public static void applyAirFrictionToPuckVelocity(Puck puck, float airFrictionCoefficient,
			float deltaTime) {
		puck.getVelocity().scl(
				(float) (Math.exp(-1 * airFrictionCoefficient * deltaTime / puck.getMass())));
	}
	
	/**
	 * Apply some restitution (i.e. inelastic loss) to puck velocity following collision.
	 * @param puck
	 * @param restitutionCoefficient The ratio of (relative speed after) / (relative speed before)
	 */
	private static void applyRestitutionToPuckVelocity(Puck puck, float restitutionCoefficient) {
		puck.getVelocity().scl(restitutionCoefficient);
	}
}