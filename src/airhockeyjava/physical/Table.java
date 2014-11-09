package airhockeyjava.physical;

/** 
 * Class to represent the air hockey table.
 * @author Joshua Segeren
 *
 */
public class Table {
	// Constants
	private static final float DEFAULT_HEIGHT_METERS = 1.8f;
	private static final float DEFAULT_WIDTH_METERS = 3f;
	
	private float width; // Defined to be the longer dimension
	private float height; // Defined to be the shorter dimension
	
	/**
	 * Default constructor.
	 */
	public Table() {
		this.height = DEFAULT_HEIGHT_METERS;
		this.width = DEFAULT_WIDTH_METERS;
	}
	
	/**
	 * Standard constructor. 
	 * @param length Longer dimension of the table.
	 * @param width Shorter dimension of the table.
	 */
	public Table(float height, float width) {
		this.height = height;
		this.width = width;
	}
	
	public float getHeight() {
		return this.height;
	}
	
	public float getWidth() {
		return this.width;
	}
}
