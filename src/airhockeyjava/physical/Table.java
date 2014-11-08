package airhockeyjava.physical;

/** 
 * Class to represent the air hockey table.
 * @author Joshua Segeren
 *
 */
public class Table {
	// Constants
	private static final float DEFAULT_LENGTH = 5;
	private static final float DEFAULT_WIDTH = 2;
	
	private float width; // Defined to be the shorter dimension
	private float length; // Defined to be the longer dimension
	
	/**
	 * Default constructor.
	 */
	public Table() {
		this.length = DEFAULT_LENGTH;
		this.width = DEFAULT_WIDTH;
	}
	
	/**
	 * Standard constructor. 
	 * @param length Longer dimension of the table.
	 * @param width Shorter dimension of the table.
	 */
	public Table(float length, float width) {
		this.length = length;
		this.width = width;
	}
	
	public float getLength() {
		return this.length;
	}
	
	public float getWidth() {
		return this.width;
	}
}
