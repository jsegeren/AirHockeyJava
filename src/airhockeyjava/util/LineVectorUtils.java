package airhockeyjava.util;

import java.awt.geom.Line2D;

/**
 * Utility class providing methods for working with both java.awt and the optimized math classes.
 * Although we want to minimize the number of times we switch between types,
 * each has specific advantages/disadvantages and we need an efficient way of converting.
 * @author Joshua Segeren
 *
 */
public class LineVectorUtils {

	public static final void scaleLine(Line2D line, float scalingFactor) {
		double x1 = line.getX1();
		double y1 = line.getY1();
		double x2 = line.getX2();
		double y2 = line.getY2();
		Vector2 scalingVector = new Vector2((float) (x2 - x1), (float) (y2 - y1))
				.scl(scalingFactor);
		line.setLine(x1, y1, x1 + scalingVector.x, y1 + scalingVector.y);
	}

	/**
	 * Creates and returns a line which starts at (x1, y1) and with a dx, dy given by the
	 * input vector.
	 * @param x1
	 * @param y1
	 * @param projection
	 * @return line
	 */
	public static final Line2D makeLineFromVector(float x1, float y1, Vector2 projection) {
		return new Line2D.Float(x1, y1, x1 + projection.x, y1 + projection.y);
	}

	/**
	 * 
	 * @param line The line to rotate
	 * @param thetaRadians Angle by which to rotate line
	 * @return rotated line
	 */
	public static final Line2D rotateLineAboutStartingPoint(Line2D line, float thetaRadians) {
		// Approach in three transformations.
		// 1) Translate start point (pivot) to origin.
		// 2) Rotate around origin by required angle.
		// 3) Translate start point back to original position.
		double x1 = line.getX1();
		double y1 = line.getY1();
		double x2 = line.getX2();
		double y2 = line.getY2();

		float newX2 = (float) (x1 + (x2 - x1) * Math.cos(thetaRadians) - (y2 - y1)
				* Math.sin(thetaRadians));
		float newY2 = (float) (y1 + (x2 - x1) * Math.sin(thetaRadians) - (y2 - y1)
				* Math.cos(thetaRadians));
		return new Line2D.Float((float) x1, (float) y1, newX2, newY2);
	}
}