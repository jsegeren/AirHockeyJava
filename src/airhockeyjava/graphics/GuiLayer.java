package airhockeyjava.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import airhockeyjava.physical.IMovingItem;
import airhockeyjava.physical.Table;

import javax.swing.JFrame; 
import javax.swing.JLabel;

import airhockeyjava.game.Game;
import airhockeyjava.game.Game.GameTypeEnum;
import airhockeyjava.physical.*;
import airhockeyjava.util.*;

/** 
 * Class for simulation UI layer.
 * @author Joshua Segeren
 *
 */

public class GuiLayer extends JFrame implements IGuiLayer {
	
	private Game game; // Reference to the top-level game object itself to have access to global variables

    private final static int FPS = 60; 
    private final static int WINDOW_WIDTH = 1024; 
    private final static int WINDOW_HEIGHT = 768; 
    
    private final static int TABLE_OFFSET_X = 80;
    private final static int TABLE_OFFSET_Y = 60;
    
    private final static int INFO_BAR_WIDTH = 200;

    private float scale = 100;

	private Set<IMovingItem> movingItems;
	private Table table;

    private BufferedImage backBuffer; 
	private boolean isRunning = true; 
    private long timer = System.currentTimeMillis();
    private long frameTime = 1000 / FPS;

    public static void main(String[] args) { 
    	    GuiLayer guiLayer = new GuiLayer(new Game(GameTypeEnum.SIMULATED_GAME_TYPE)); 
            guiLayer.start();
            System.exit(0); 
    } 

	public GuiLayer(Game game) {
		this.game = game;
	}

	/* (non-Javadoc)
	 * @see airhockeyjava.graphics.IGuiLayer#start()
	 */
	public void start() {
		// Provide loop based on desired refresh rate to render output based on item positions
        initialize(); 
        
        while(isRunning) 
        { 
                long currentTime = System.currentTimeMillis(); 
                if(currentTime - timer > frameTime){
                    draw();
                    timer += frameTime;
                }                
        } 
        
        setVisible(false); 
	}
	
	// TODO should run and start be the same method?
	public void run() {}
    
    /** 
     * Open a window and setup to begin drawing
     */ 
    void initialize() 
    { 
            setTitle("AirHockey"); 
            setSize(WINDOW_WIDTH, WINDOW_HEIGHT); 
            setResizable(false); 
            setDefaultCloseOperation(EXIT_ON_CLOSE); 
            setVisible(true); 
           
            setScale();
            
            backBuffer = new BufferedImage(WINDOW_WIDTH, WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);             
    }
       
    /** 
     * Draw all objects to the screen 
     */ 
    void draw() 
    {               
            Graphics graphicsContext = getGraphics(); 
            Graphics bufferContext = backBuffer.getGraphics(); 
            
            bufferContext.setColor(Color.BLACK); 
            bufferContext.fillRect(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT); 

            drawTable(game.gameTable, bufferContext, Color.WHITE);
            
            Iterator<IMovingItem> iter = this.movingItems.iterator();
            while(iter.hasNext()){
            	drawMovingItem(iter.next(), bufferContext, Color.WHITE);	
            }
            
            graphicsContext.drawImage(backBuffer, 0, 0, this); 
    }
 
    /** 
     * Draw a table
     * @param item
     * @param buffer  
     */
    private void drawTable(Table table, Graphics context, Color color){
        context.setColor(Color.WHITE); 
        context.drawRect(TABLE_OFFSET_X, TABLE_OFFSET_Y, (int)scale(table.getWidth()), (int)scale(table.getLength())); 

    }

    /** 
     * Draw a MovingItem to the table
     * @param item
     * @param buffer  
     */
    private void drawMovingItem(IMovingItem item, Graphics context, Color color){
        context.setColor(color); 
    	Vector2 position = item.getPosition();
    	float radius = scale(item.getRadius());
    	context.drawOval(
    			(int)scale(position.x) + this.TABLE_OFFSET_X, 
    			(int)scale(position.y) + this.TABLE_OFFSET_Y, 
    			(int)scale(radius), 
    			(int)scale(radius));
    }

    /** 
     * Scale a value from meters to pixels
     * @param value  
     */
    private float scale(float value){
    	return value * this.scale;
    }
    
    /*
     * Set the scaling factor of the display based on the table length
     */
    private void setScale(){
    	this.scale = (WINDOW_WIDTH - INFO_BAR_WIDTH - (TABLE_OFFSET_X * 2)) / game.gameTable.getWidth();
    	System.out.println(this.scale);
    }
    
    private class InfoBar {
    	
    	private int width = 0;
    	private int height = 0;
    	private int lineHeight = 20;
    	private int fontSize = 10;
    	private Graphics context;
    	private int cursor = 0;
    	
    	InfoBar(int width, int hieght, Graphics context){
    		this.width = width; 
    		this.height = height;
    		this.context = context;
    	}
 
        /** 
         * Draw an info bar on the right hand side of the screen
         * @param item
         * @param buffer  
         */
        private void drawLine(){
            this.context.setColor(Color.WHITE); 
            int startX = INFO_BAR_WIDTH;
            int startY = TABLE_OFFSET_Y;   
        }	
    }

}