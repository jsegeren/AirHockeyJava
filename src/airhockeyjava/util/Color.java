package airhockeyjava.util;

/**
 * Enumeration type to distinguish items for detection and rendering.
 * @author Joshua Segeren
 */
public enum Color {
	RED ("red"),
	BLUE ("blue"),
	BLACK ("black");
	
	private String colorString;
	
	Color(String colorString) {
		this.colorString = colorString;
	}
	
	public String getColorString() {
		return this.colorString;
	}
}