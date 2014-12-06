package airhockeyjava.simulation;

import java.awt.geom.Line2D;

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
	 * Static method to check if two moving items are colliding
	 * @param itemA
	 * @param itemB
	 * @return
	 */
	public static boolean isColliding(IMovingItem itemA, IMovingItem itemB) {
		return ((itemA != itemB) && new Vector2(itemA.getPosition()).sub(itemB.getPosition()).len() <= itemA
				.getRadius() + itemB.getRadius());
	}

	public static boolean hasCollided(IMovingItem itemA, IMovingItem itemB) {
		Line2D trajectoryLineA = itemA.getTrajectoryLine();
		Line2D trajectoryLineB = itemB.getTrajectoryLine();
		float squareLengthA = (float) (Math.pow(trajectoryLineA.getX2() - trajectoryLineA.getX1(),
				2) + Math.pow(trajectoryLineA.getY2() - trajectoryLineA.getY1(), 2));
		float squareLengthB = (float) (Math.pow(trajectoryLineB.getX2() - trajectoryLineB.getX1(),
				2) + Math.pow(trajectoryLineB.getY2() - trajectoryLineB.getY1(), 2));
		return ((squareLengthA != 0f) && (squareLengthB != 0f) && itemA.getTrajectoryLine()
				.intersectsLine(itemB.getTrajectoryLine()));
	}

	/**
	 * Static method to handle collision between puck and mallet.
	 * Derivation based on resource: http://www.vobarian.com/collisions/2dcollisions2.pdf
	 * @param puck
	 * @param mallet
	 * @return Vector2 the new velocity of the puck
	 */
	public static Vector2 handleCollision(Puck puck, Mallet mallet) {
		// Correct position of puck to prevent cascading/duplicate collisions
		Vector2 unitNormalVector = new Vector2(puck.getPosition()).sub(mallet.getPosition()).nor();
		puck.setPosition(new Vector2(mallet.getPosition()).add(new Vector2(unitNormalVector)
				.scl(puck.getRadius() + mallet.getRadius())));

		// Find the unit tangent vector
		Vector2 unitTangentVector = new Vector2(unitNormalVector).rotate90(0);

		// After collision, tangential components of velocities are unchanged.
		// Normal component of velocities can be found using the one-dimensional collision
		// formulas. Need to resolve velocity vectors v1, v2 into normal and tangential components.
		// To do this, project velocity vectors onto unit normal and unit tangent vectors by computing
		// the dot product.
		float puckSpeedNormal = unitNormalVector.dot(puck.getVelocity());
		float malletSpeedNormal = unitNormalVector.dot(mallet.getVelocity());
		float puckSpeedTangent = unitTangentVector.dot(puck.getVelocity());
		//		float malletSpeedTangent = unitTangentVector.dot(mallet.getVelocity());

		float newPuckSpeedNormal;
		// If mallet is infinite mass, simplify equation and short-circuit
		if (mallet.getMass() == Float.MAX_VALUE) {
			newPuckSpeedNormal = 2f * malletSpeedNormal - puckSpeedNormal;
		} else {
			// New tangent components = old tangent components!
			// Find new normal velocities using one-dimensional collision formulas. (Velocities
			// of two circles along normal direction are perpendicular to surfaces of circles at point
			// of collision, so it's a one-dimensional collision.)
			newPuckSpeedNormal = (puckSpeedNormal * (puck.getMass() - mallet.getMass()) + (2f * mallet
					.getMass() * malletSpeedNormal)) / (puck.getMass() + mallet.getMass());

		}
		// Convert scalar normal and tangential velocities into vectors by multiplying unit normal vector by
		// scalar normal velocity; similar for tangential.
		Vector2 newPuckNormalVelocity = new Vector2(unitNormalVector).scl(newPuckSpeedNormal);
		Vector2 newPuckTangentVelocity = new Vector2(unitTangentVector).scl(puckSpeedTangent);

		// Finally, find final velocity vectors by adding normal and tangential components for each object.
		return new Vector2(newPuckNormalVelocity).add(newPuckTangentVelocity);

	}
}