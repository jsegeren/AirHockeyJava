package airhockeyjava.input;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;

import airhockeyjava.game.Constants;
import airhockeyjava.graphics.GuiLayer;

/**
 * Makes handling input a lot simpler
 */
public class InputLayer implements IInputLayer {
	Point p;
	private int mouseX;
	private int mouseY;

	public InputLayer(GuiLayer guiLayer) {
		super();
		guiLayer.addMouseListener(this);
		guiLayer.addMouseMotionListener(this);
	}

	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent me) {
	}

	@Override
	public void mouseExited(MouseEvent me) {
	}

	@Override
	public void mousePressed(MouseEvent me) {
		p = me.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent me) {
		p = null;
	}

	@Override
	public void mouseDragged(MouseEvent me) {
		p = me.getPoint();
	}

	@Override
	public int getMouseX() {
		return this.mouseX;
	}

	@Override
	public int getMouseY() {
		return this.mouseY;
	}

	@Override
	public void mouseMoved(MouseEvent me) {
		mouseX = me.getX();
		mouseY = me.getY();
	}
}