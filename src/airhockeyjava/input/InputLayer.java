package airhockeyjava.input;

import java.awt.Point;
import java.awt.event.MouseEvent;

import airhockeyjava.graphics.GuiLayer;

/**
 * Makes handling input a lot simpler
 */
public class InputLayer implements IInputLayer {
	Point p;
	private int mouseX;
	private int mouseY;

	public InputLayer(GuiLayer guiLayer) {
		guiLayer.addMouseListener(this);
		guiLayer.addMouseMotionListener(this);
	}

	public void mouseClicked(MouseEvent me) {
	}

	public void mouseEntered(MouseEvent me) {
	}

	public void mouseExited(MouseEvent me) {
	}

	public void mousePressed(MouseEvent me) {
		p = me.getPoint();
	}

	public void mouseReleased(MouseEvent me) {
		p = null;
	}

	public void mouseDragged(MouseEvent me) {
		p = me.getPoint();
	}

	public int getMouseX() {
		return this.mouseX;
	}

	public int getMouseY() {
		return this.mouseY;
	}

	public void mouseMoved(MouseEvent me) {
		mouseX = me.getX();
		mouseY = me.getY();
	}

	public void run() {
	}

}