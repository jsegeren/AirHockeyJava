package airhockeyjava.input;

import java.awt.event.MouseEvent;
import javax.swing.JPanel;

/**
 * Makes handling input a lot simpler
 */
public class InputLayer implements IInputLayer {

	private JPanel jpanel;
	private int mouseX = 0; // Pixel x coordinate
	private int mouseY = 0; // Pixel y coordinate

	public InputLayer(JPanel jframe) {
		this.jpanel = jpanel;
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

	public void mouseDragged(MouseEvent e) {
	};

	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void run() {
		jpanel.addMouseMotionListener(this);
	}

}