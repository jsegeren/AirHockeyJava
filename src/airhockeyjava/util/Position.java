package airhockeyjava.util;

/** 
 * Class to represent position in appropriate coordinate system
 * @author Joshua Segeren
 */
public class Position extends Vector2 {
	
	private static final float DEFAULT_X = 0;
	private static final float DEFAULT_Y = 0;
	
	/**
	 * Enumeration type to distinguish the coordinate systems we may use.
	 * Note that additional member variables may be added to the enum type
	 * (e.g. coordinate ranges, origin positions, fixed precisions).
	 *
	 */
	public enum PositionCoordinateSystem {
		CAMERA_COORDINATE_SYSTEM,
		ROBOT_COORDINATE_SYSTEM
	}
	
	private PositionCoordinateSystem positionCoordinateSystem;
	
	/**
	 * The robust constructor
	 */
	public Position(Vector2 p, PositionCoordinateSystem positionCoordinateSystem) {
		super(p != null ? p.x : DEFAULT_X, p!= null ? p.y : DEFAULT_Y);
		this.positionCoordinateSystem = positionCoordinateSystem != null ? 
				positionCoordinateSystem : PositionCoordinateSystem.CAMERA_COORDINATE_SYSTEM;
	}
}