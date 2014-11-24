/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package airhockeyjava.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * A set of utility to compute intersection of line segments, creating arrows etc.
 * 
 * @author Pinaki Poddar
 *
 */
public class Geometry {
	static final boolean _debug = Boolean.getBoolean("graph.debug");

	/**
	 * Computes the intersection of the given line segment with the given rectangle.
	 * 
	 * @return null if the line segment does not intersect the rectangle.
	 */
	public static Point2D intersection(Line2D seg, Rectangle2D r) {
		return intersection(seg.getP1(), seg.getP2(), r);
	}

	/**
	 * Computes the intersection of the line segment represented by the points with the given rectangle.
	 * 
	 * @return null if the line segment does not intersect the rectangle.
	 */
	public static Point2D intersection(Point2D p1, Point2D p2, Rectangle2D r) {
		return intersection(p1.getX(), p1.getY(), p2.getX(), p2.getY(), r);
	}

	/**
	 * Computes the intersection of the line segment represented by the points with the given rectangle.
	 * 
	 * @return null if the line segment does not intersect the rectangle.
	 */
	public static Point2D intersection(double x1, double y1, double x2, double y2, Rectangle2D r) {
		Point2D p = null;
		p = intersection(x1, y1, x2, y2, r.getX(), r.getY(), r.getX(), r.getY() + r.getHeight());
		if (p != null)
			return p;
		p = intersection(x1, y1, x2, y2, r.getX(), r.getY(), r.getX() + r.getWidth(), r.getY());
		if (p != null)
			return p;
		p = intersection(x1, y1, x2, y2, r.getX() + r.getWidth(), r.getY() + r.getHeight(),
				r.getX(), r.getY() + r.getHeight());
		if (p != null)
			return p;
		p = intersection(x1, y1, x2, y2, r.getX() + r.getWidth(), r.getY() + r.getHeight(),
				r.getX() + r.getWidth(), r.getY());
		return p;
	}

	/**
	 * Computes intersection between the given line segments.
	 * 
	 * @return null if the line segments do not intersects.
	 */
	public static Point2D intersection(double x1, double y1, double x2, double y2, double x3,
			double y3, double x4, double y4) {
		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		if (denom == 0) {
			return null;
		}
		double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
		double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
		if (isBounded(ua, 0.0, 1.0) && isBounded(ub, 0.0, 1.0)) {
			return new Point2D.Double(x1 + ua * (x2 - x1), y1 + ua * (y2 - y1));
		}
		return null;
	}

	/**
	 * Affirms if the given value is strictly bounded by the given range.
	 */
	public static boolean isBounded(double a, double min, double max) {
		return a > min && a < max;
	}

	/**
	 * Gets the first two points of the given path.
	 * 
	 * @param path
	 * @return initial line
	 */
	public static Line2D getStartpoints(Path2D path) {
		double[] points = new double[6];
		Point2D p1 = null;
		Point2D p2 = null;
		PathIterator iter = path.getPathIterator(null);
		iter.currentSegment(points);
		p1 = new Point2D.Double(points[0], points[1]);
		iter.next();
		iter.currentSegment(points);
		p2 = new Point2D.Double(points[0], points[1]);
		return new Line2D.Double(p1, p2);
	}

	/**
	 * Gets the last two points of the given path.
	 * 
	 * @param path
	 * @return final line
	 */
	public static Line2D getEndpoints(Path2D path) {
		AffineTransform at = new AffineTransform();
		double[] points = new double[6];
		Point2D p1 = null;
		Point2D p2 = null;
		PathIterator iter = path.getPathIterator(at);
		while (!iter.isDone()) {
			iter.currentSegment(points);
			p1 = p2;
			p2 = new Point2D.Double(points[0], points[1]);
			iter.next();
		}
		return new Line2D.Double(p1, p2);
	}

	/**
	 * Gets the point on the periphery of given rectangle to meet the given outside point. 
	 * 
	 * @return null if the point is inside the rectangle.
	 */
	public static Point2D exitPoint(Rectangle2D r, double x, double y) {
		if (r.contains(x, y))
			return null;
		int o = r.outcode(x, y);
		double w = r.getWidth();
		double h = r.getHeight();
		double x0 = r.getCenterX();
		double y0 = r.getCenterY();

		if ((o & Rectangle2D.OUT_LEFT) != 0)
			x0 -= w / 2;
		if ((o & Rectangle2D.OUT_RIGHT) != 0)
			x0 += w / 2;
		if ((o & Rectangle2D.OUT_TOP) != 0)
			y0 -= h / 2;
		if ((o & Rectangle2D.OUT_BOTTOM) != 0)
			y0 += h / 2;

		return new Point2D.Double(x0, y0);
	}

	/**
	 * Get a path between the centers of two given rectangles.
	 * The path has two orthogonal line segments. The first segment is horizontal (vertical)
	 * if the horizontal displacement between the centers is longer (shorter) than the vertical.
	 */
	public static Path2D getRectilinearPath(Rectangle2D r1, Rectangle2D r2) {
		return getRectilinearPath(r1.getCenterX(), r1.getCenterY(), r2.getCenterX(),
				r2.getCenterY());
	}

	public static Path2D getRectilinearPath(Point2D start, Point2D end) {
		return getRectilinearPath(start.getX(), start.getY(), end.getX(), end.getY());
	}

	public static Path2D getRectilinearPath(double x1, double y1, double x2, double y2) {
		Path2D path = new Path2D.Double();
		path.moveTo(x1, y1);
		if (Math.abs(x1 - x2) > Math.abs(y1 - y2)) {
			path.lineTo(x2, y1);
		} else {
			path.lineTo(x1, y2);
		}
		path.lineTo(x2, y2);
		return path;
	}

	/**
	 * Given a pair of rectangles find the path that connects them without intersecting them.
	 */
	public static Path2D getConnector(Rectangle2D r1, Rectangle2D r2) {
		Path2D full = getRectilinearPath(r1, r2);

		Line2D exitPin = getStartpoints(full);
		Line2D entryPin = getEndpoints(full);
		return getConnector(exitPin, entryPin);
	}

	public static Path2D getConnector(Rectangle2D r1, Line2D entryPin) {
		Point2D end = entryPin.getP1();
		Path2D full = getRectilinearPath(r1.getCenterX(), r1.getCenterY(), end.getX(), end.getY());

		Line2D exitPin = getStartpoints(full);
		return getConnector(exitPin, entryPin);
	}

	public static Path2D getConnector(Line2D exitPin, Rectangle2D r2) {
		Path2D full = getRectilinearPath(exitPin.getP2().getX(), exitPin.getP2().getY(),
				r2.getCenterX(), r2.getCenterY());

		Line2D entryPin = getEndpoints(full);
		return getConnector(exitPin, entryPin);
	}

	/**
	 * Connect a path between two given pins.
	 */
	public static Path2D getConnector(Line2D exitPin, Line2D entryPin) {
		Path2D path = new Path2D.Double();
		Point2D start = exitPin.getP1();
		path.moveTo(start.getX(), start.getY());
		start = exitPin.getP2();
		path.lineTo(start.getX(), start.getY());
		log("Final Path : " + start);
		int b = 6;
		Point2D end = entryPin.getP1();
		boolean xStraight = Math.abs(start.getX() - end.getX()) < b;
		boolean yStraight = Math.abs(start.getY() - end.getY()) < b;
		boolean straight = xStraight || yStraight;
		if (straight) {
			if (xStraight) {
				path.lineTo(end.getX(), start.getY());
			} else if (yStraight) {
				path.lineTo(start.getX(), end.getY());
			}
			path.lineTo(end.getX(), end.getY());
			path.lineTo(entryPin.getP2().getX(), entryPin.getP2().getY());
			return path;
		}
		double bx = Math.signum(start.getX() - end.getX()) * b;
		double by = Math.signum(start.getY() - end.getY()) * b;
		if (Math.abs(start.getX() - end.getX()) > Math.abs(start.getY() - end.getY())) {
			path.lineTo(end.getX() + bx, start.getY());
			path.quadTo(end.getX() + bx / 3, start.getY() - by / 3, end.getX(), start.getY() - by);
			log("-> [" + end.getX() + "," + start.getY() + "]");
		} else {
			path.lineTo(start.getX(), end.getY() + by);
			path.quadTo(start.getX() - bx / 3, end.getY() + by / 3, start.getX() - bx, end.getY());
			log("-> [" + start.getY() + "," + end.getY() + "]");
		}
		path.lineTo(end.getX(), end.getY());
		path.lineTo(entryPin.getP2().getX(), entryPin.getP2().getY());
		return path;
	}

	/**
	 * Get the slope of the given points in radian. 
	 */
	public static double getAngle(Point2D p1, Point2D p2) {
		return -Math.atan2(p1.getX() - p2.getX(), p1.getY() - p2.getY());
	}

	/**
	 * Gets an arrow shape of given size at the end of the given path.
	 */
	public static Shape addEndArrow(Path2D path, double arrowSize, double arrowAngleInDegree) {
		Line2D end = Geometry.getEndpoints(path);
		return addArrow(end.getP1(), end.getP2(), arrowSize, arrowAngleInDegree);
	}

	/**
	 * Creates an arrow shape of given size at the beginning of the given path.
	 */
	public static Shape addStartArrow(Path2D path, double arrowSize, double arrowAngleInDegree) {
		Line2D start = Geometry.getStartpoints(path);
		return addArrow(start.getP2(), start.getP1(), arrowSize, arrowAngleInDegree);
	}

	/**
	 * Creates an arrow for the line segment p1,p2 whose tip is at p2.
	 * @param p1 the beginning of the line segment
	 * @param p2 the end of the line segment
	 * @param scale the projected length of the arrow along the line segment
	 * @return
	 */
	public static Shape addArrow(Point2D p1, Point2D p2, double arrowSize, double arrowAngleInDegree) {
		Path2D arrow = createArrow(arrowAngleInDegree);
		AffineTransform at = AffineTransform.getTranslateInstance(p2.getX(), p2.getY());
		double angle = getAngle(p1, p2);
		at.rotate(angle);
		at.scale(arrowSize, arrowSize);
		Shape shape = at.createTransformedShape(arrow);
		return shape;
	}

	/**
	 * Creates an arrow whose tip is at (0,0), unit height and angled theta.
	 * @return
	 */
	public static Path2D.Double createArrow(double arrowAngleInDegree) {
		double L = Math.tan(Math.toRadians(arrowAngleInDegree));
		Path2D.Double path = new Path2D.Double();
		path.moveTo(0, 0);
		path.lineTo(-L, 1);
		path.lineTo(L, 1);
		path.lineTo(0, 0);

		return path;
	}

	public static Point asPoint(Point2D p) {
		return new Point((int) p.getX(), (int) p.getY());
	}

	public static Rectangle asRect(Rectangle2D r) {
		return new Rectangle((int) r.getX(), (int) r.getY(), (int) r.getWidth(),
				(int) r.getHeight());
	}

	public static void printPath(Path2D path) {
		double[] points = new double[6];
		PathIterator iter = path.getPathIterator(null);
		while (!iter.isDone()) {
			int type = iter.currentSegment(points);
			switch (type) {
			case PathIterator.SEG_MOVETO:
				System.err.print("[" + points[0] + "," + points[1] + "]");
				break;
			case PathIterator.SEG_LINETO:
				System.err.print("->[" + points[0] + "," + points[1] + "]");
				break;
			case PathIterator.SEG_QUADTO:
				System.err.print("C[" + points[0] + "," + points[1] + ":" + points[2] + ","
						+ points[3] + "]");
				break;
			default:
			}
			iter.next();
		}
		System.err.println();
	}

	/**
	 * Modifies the given path such that it does not intersect the given rectangle. 
	 */
	public static Path2D detour(Path2D path, Rectangle2D r) {
		Rectangle2D i = path.getBounds2D().createIntersection(r);
		if (i.getWidth() == 0 || i.getHeight() == 0)
			return path;
		BitSet bits = insider(i, r);
		if (bits.isEmpty())
			return path;
		if (bits.cardinality() == 1) {
			int n1 = bits.nextSetBit(0);
			int n2 = (n1 + 2) % 4;
			Point2D p = mirror(n2, r, 20);
			return modify(path, vertex(n1, i), p, n2);
		}
		return path;

	}

	/**
	 * Find which vertex(s) of i is inside r if any.
	 * Ignore if vertex is on edge of r
	 */
	public static BitSet insider(Rectangle2D in, Rectangle2D r) {
		BitSet bits = new BitSet(4);
		for (int i = 0; i < 4; i++) {
			Point2D v = vertex(i, in);
			bits.set(
					i,
					v.getX() > r.getX() && v.getX() < (r.getX() + r.getWidth())
							&& v.getY() > r.getY() && v.getY() < (r.getY() + r.getHeight()));
		}
		return bits;
	}

	public static Point2D mirror(int i, Rectangle2D r, int s) {
		Point2D m = vertex(i, r);
		AffineTransform tr = new AffineTransform();
		tr.translate(i == 0 || i == 3 ? -s : s, i == 0 || i == 1 ? -s : s);
		return tr.transform(m, m);
	}

	/**
	 * Gets the i-th vertex of the given rectangle.
	 * 0-th vertex is top-left, and counted couter-clockwise i.e bottom-left is at index 3.
	 */
	public static Point2D vertex(int i, Rectangle2D r) {
		return new Point2D.Double(r.getX() + (i == 1 || i == 2 ? r.getWidth() : 0), r.getY()
				+ (i == 2 || i == 3 ? r.getHeight() : 0));
	}

	public static Path2D modify(Path2D original, Point2D breakPoint, Point2D newPoint, int vidx) {
		List<Point2D> pts = getLineSegments(original);
		int i = pts.indexOf(breakPoint);
		Point2D p0 = pts.get(i - 1);
		Point2D p1 = pts.get(i);
		Point2D p2 = pts.get(i + 1);

		Point2D[] qs = new Point2D[5];
		qs[0] = p0;
		qs[1] = new Point2D.Double(vidx == 0 ? newPoint.getX() : p0.getX(), vidx == 0 ? p0.getY()
				: newPoint.getY());
		qs[2] = newPoint;
		qs[3] = new Point2D.Double(vidx == 0 ? p2.getX() : newPoint.getX(),
				vidx == 0 ? newPoint.getY() : p2.getY());
		qs[4] = p2;
		return createPath(qs);
	}

	public static List<Point2D> getLineSegments(Path2D original) {
		List<Point2D> pts = new ArrayList<Point2D>();
		PathIterator iterator = original.getPathIterator(null);
		double[] coords = new double[6];
		while (!iterator.isDone()) {
			int segtype = iterator.currentSegment(coords);
			// assume all straight segments
			pts.add(new Point2D.Double(coords[0], coords[1]));
			iterator.next();
		}
		return pts;
	}

	public static Path2D createPath(List<Point2D> pts) {
		Path2D path = new Path2D.Double();
		for (int i = 0; i < pts.size(); i++) {
			Point2D p = pts.get(i);
			if (i == 0) {
				path.moveTo(p.getX(), p.getY());
			} else {
				path.lineTo(p.getX(), p.getY());
			}
		}
		return path;
	}

	public static Path2D createPath(Point2D[] pts) {
		Path2D path = new Path2D.Double();
		for (int i = 0; i < pts.length; i++) {
			Point2D p = pts[i];
			if (i == 0) {
				path.moveTo(p.getX(), p.getY());
			} else {
				path.lineTo(p.getX(), p.getY());
			}
		}
		return path;
	}

	static void log(String s) {
		if (_debug)
			System.err.println(s);
	}

}