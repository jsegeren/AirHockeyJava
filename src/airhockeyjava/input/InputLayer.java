package airhockeyjava.input;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.EmptyStackException;
import java.util.Stack;

import airhockeyjava.graphics.GuiLayer;

/**
 * Makes handling input a lot simpler
 */
public class InputLayer implements IInputLayer {
	Point p;
	private int mouseX;
	private int mouseY;
	private Stack<Integer> unhandledKeyPresses = new Stack<Integer>();

	public InputLayer(GuiLayer guiLayer) {
		guiLayer.addMouseListener(this);
		guiLayer.addMouseMotionListener(this);
		guiLayer.addKeyListener(this);
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

	@Override
	public void run() {
	}

	@Override
	public void keyTyped(KeyEvent ke) {
	}

	@Override
	public void keyPressed(KeyEvent ke) {
		System.out.println(ke.toString());
		this.unhandledKeyPresses.push(ke.getKeyCode());
	}

	@Override
	public void keyReleased(KeyEvent ke) {
	}

	/**
	 * Returns the last key pressed or VK_UNDEFINED if all keypresses have been handled
	 */
	@Override
	public int handleKeyPress() {
		try {
			return this.unhandledKeyPresses.pop();
		} catch (EmptyStackException e) {
			return KeyEvent.VK_UNDEFINED;
		}
	}

}