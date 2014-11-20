package airhockeyjava.simulation;

import airhockeyjava.game.Constants;
import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Mallet;
import airhockeyjava.physical.Puck;
import airhockeyjava.util.Vector2;

/**
 * Class with static methods for evaluating current or future collisions between physical entities.
 * @author Joshua Segeren
 *
 */
public class Collision {

	/**
	 * Static method to check whether two moving items will collide, given their current trajectories, and
	 * expected friction.
	 * 
	 * @param itemA
	 * @param itemB
	 * @param deltaT relative time over which collision may occur
	 * @return true if collision imminent
	 */
	public static boolean willCollide(IMovingItem itemA, IMovingItem itemB, float deltaT) {
		return true; // TODO actually implement this
	}

	/**
	 * Static method to check if two moving items are colliding
	 * @param itemA
	 * @param itemB
	 * @return
	 */
	public static boolean isColliding(IMovingItem itemA, IMovingItem itemB) {
		return ((itemA != itemB) && new Vector2(itemA.getPosition()).sub(itemB.getPosition()).len() <= itemA
				.getRadius() + itemB.getRadius());
	}

	/**
	 * Static method to handle collision between puck and mallet.
	 * TODO Account for mass differences between puck and mallet. Use energy formulation instead.
	 * @param puck
	 * @param mallet
	 * @return Vector2 the new velocity of the puck
	 */
	public static Vector2 handleCollision(Puck puck, Mallet mallet) {
		// Correct position of puck to prevent cascading/duplicate collisions
		Vector2 unitVectorBetween = new Vector2(puck.getPosition()).sub(mallet.getPosition()).nor();
		puck.setPosition(new Vector2(mallet.getPosition()).add(unitVectorBetween.scl(puck
				.getRadius() + mallet.getRadius())));

		Vector2 malletToPuck = getTransferredForce(mallet, puck);
		Vector2 newPuckVelocity = new Vector2(malletToPuck).sub(puck.getVelocity());
		return newPuckVelocity.scl(1 - Constants.MALLET_PUCK_COLLISION_LOSS_COEFFICIENT).limit(
				Constants.MAX_PUCK_SPEED_METERS_PER_SECOND);

	}

	/**
	 * Static method to get transferred (expected) force between two moving items.
	 * @param itemA
	 * @param itemB
	 * @return
	 */
	private static Vector2 getTransferredForce(IMovingItem itemA, IMovingItem itemB) {
		Vector2 unitVectorBetween = new Vector2(itemA.getPosition()).sub(itemB.getPosition()).nor();
		Vector2 velocityA = new Vector2(itemA.getVelocity());

		if (!velocityA.equals(Vector2.Zero)) {
			// Consider angle between two items and velocity
			float collisionAngle = (float) Vector2.angleBetween(unitVectorBetween, velocityA);
			if (collisionAngle > 90f) {
				float impactOther = (collisionAngle - 90f) / 90f;
				float forceOther = velocityA.len() * impactOther;
				return unitVectorBetween.scl(forceOther).scl(-1f);
			}
		}
		return new Vector2();
	}
}