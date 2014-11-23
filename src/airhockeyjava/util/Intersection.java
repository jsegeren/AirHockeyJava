package airhockeyjava.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Utlity class providing methods for finding intersections.
 * @author Joshua Segeren
 *
 */
public class Intersection {
	public static Point2D[] getIntersectionPoints(Line2D line, Rectangle2D rectangle) {
		Point2D[] p = new Point2D[4];

		// Top line
		p[0] = getTopIntersectionPoint(line, rectangle);
		// Bottom line
		p[1] = getBottomIntersectionPoint(line, rectangle);
		// Left side...
		p[2] = getLeftIntersectionPoint(line, rectangle);
		// Right side
		p[3] = getRightIntersectionPoint(line, rectangle);

		return p;
	}

	/** 
	 * Special method to find intersection of trajectory line (in direction of movement) with
	 * the edges of the rectangle that contain it.
	 * @param line
	 * @param rectangle
	 * @return Single intersection point
	 */
	public static Point2D getIntersectionPoint(Line2D line, Rectangle2D rectangle) {
		double x1 = line.getX1();
		double y1 = line.getY1();
		double x2 = line.getX2();
		double y2 = line.getY2();

		Point2D intersectionPoint = null;

		// Only get the appropriate intersection point
		if (x1 < x2 && y1 < y2) {
			intersectionPoint = getRightIntersectionPoint(line, rectangle);
			if (intersectionPoint == null) {
				intersectionPoint = getBottomIntersectionPoint(line, rectangle);
			}
		} else if (x1 < x2 && y1 > y2) {
			intersectionPoint = getRightIntersectionPoint(line, rectangle);
			if (intersectionPoint == null) {
				intersectionPoint = getTopIntersectionPoint(line, rectangle);
			}
		} else if (x1 > x2 && y1 < y2) {
			intersectionPoint = getLeftIntersectionPoint(line, rectangle);
			if (intersectionPoint == null) {
				intersectionPoint = getBottomIntersectionPoint(line, rectangle);
			}
		} else if (x1 > x2 && y1 > y2) {
			intersectionPoint = getLeftIntersectionPoint(line, rectangle);
			if (intersectionPoint == null) {
				intersectionPoint = getTopIntersectionPoint(line, rectangle);
			}
		}

		return intersectionPoint;
	}

	private static Point2D getBottomIntersectionPoint(Line2D line, Rectangle2D rectangle) {
		return getIntersectionPoint(line, new Line2D.Double(rectangle.getX(), rectangle.getY()
				+ rectangle.getHeight(), rectangle.getX() + rectangle.getWidth(), rectangle.getY()
				+ rectangle.getHeight()));
	}

	private static Point2D getTopIntersectionPoint(Line2D line, Rectangle2D rectangle) {
		return getIntersectionPoint(line, new Line2D.Double(rectangle.getX(), rectangle.getY(),
				rectangle.getX() + rectangle.getWidth(), rectangle.getY()));
	}

	private static Point2D getLeftIntersectionPoint(Line2D line, Rectangle2D rectangle) {
		return getIntersectionPoint(line, new Line2D.Double(rectangle.getX(), rectangle.getY(),
				rectangle.getX(), rectangle.getY() + rectangle.getHeight()));
	}

	private static Point2D getRightIntersectionPoint(Line2D line, Rectangle2D rectangle) {
		return getIntersectionPoint(line, new Line2D.Double(
				rectangle.getX() + rectangle.getWidth(), rectangle.getY(), rectangle.getX()
						+ rectangle.getWidth(), rectangle.getY() + rectangle.getHeight()));
	}

	public static Point2D getIntersectionPoint(Line2D lineA, Line2D lineB) {
		if (!lineA.intersectsLine(lineB)) {
			return null;
		}

		double x1 = lineA.getX1();
		double y1 = lineA.getY1();
		double x2 = lineA.getX2();
		double y2 = lineA.getY2();

		double x3 = lineB.getX1();
		double y3 = lineB.getY1();
		double x4 = lineB.getX2();
		double y4 = lineB.getY2();

		Point2D p = null;

		double d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
		if (d != 0) {
			double xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
			double yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;

			p = new Point2D.Double(xi, yi);

		}
		return p;
	}
}