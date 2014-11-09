package airhockeyjava.input;

import java.awt.event.*; 
import javax.swing.JFrame;

/** 
 * Makes handling input a lot simpler 
 */ 
public class InputLayer implements MouseMotionListener {   
	
	private int mouseX = 0;
	private int mouseY = 0;
	
	public InputLayer(JFrame frame){
		frame.addMouseMotionListener(this);
	}
	
	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

    public void mouseDragged(MouseEvent e){
    };
    
    public void mouseMoved(MouseEvent e){
    	mouseX = e.getX();	
    	mouseY = e.getY();
    }

} 