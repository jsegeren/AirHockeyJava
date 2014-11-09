package airhockeyjava.input;

import java.awt.event.MouseEvent;
import javax.swing.JFrame;

/** 
 * Makes handling input a lot simpler 
 */ 
public class InputLayer implements IInputLayer {   
	
	private JFrame jframe;
	private int mouseX = 0; // Pixel x coordinate
	private int mouseY = 0; // Pixel y coordinate
	
	public InputLayer(JFrame jframe){
		this.jframe = jframe;
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
    
    public void run() {
    	jframe.addMouseMotionListener(this);
    }

} 