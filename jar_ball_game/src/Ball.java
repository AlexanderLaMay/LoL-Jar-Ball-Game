// Ball.java
import java.awt.*;
import java.awt.image.BufferedImage;

// Represents a single ball in the game
class Ball {
    int x, y;
    double radius;
    BufferedImage image;
    int level; // Represents the size tier of the ball
    int value; // Points for this ball
    long outOfBoundsTimerStart = 0L;
    
    //Velocity Components
    double vx = 0;
    double vy = 0;
    
    //Mass of the ball
    double mass;
    
    // Factor to make the image drawn larger than the ball's diameter.
    private static final double IMAGE_DISPLAY_SCALE_FACTOR = 1.2;

    // Constructor for a new ball
    public Ball(int x, int y, int level, BufferedImage image) {
        this.x = x;
        this.y = y;
        this.level = level;
        this.image = image;
        assignPropertiesByLevel(level);
        this.mass = radius * radius;
    }

    // Assigns radius, color, and value based on the ball's level
    private void assignPropertiesByLevel(int level) {
        switch (level) {
            case 0: // Smallest
                this.radius = 15;
                this.value = 10;
                break;
            case 1:
                this.radius = 22.5;
                this.value = 20;
                break;
            case 2:
                this.radius = 30;
                this.value = 40;
                break;
            case 3:
                this.radius = 37.5;
                this.value = 80;
                break;
            case 4:
                this.radius = 46;
                this.value = 160;
                break;
            case 5: 
                this.radius = 53.5;
                this.value = 320;
                break;
            case 6:
                this.radius = 60;
                this.value = 320;
                break;
            case 7: // Largest size
                this.radius = 67.5;
                this.value = 320;
                break;
        }
    }

    // Draws the ball on the graphics context
    public void draw(Graphics g) {
    	//Draw the image scaled to the ball's radius
        if (image != null) {
        	int imageDrawDiameter = (int) (radius * 2 * IMAGE_DISPLAY_SCALE_FACTOR);
        	
        	int imageDrawX = x - (imageDrawDiameter / 2);
        	int imageDrawY = y - (imageDrawDiameter / 2);
        	
        	g.drawImage(image, imageDrawX, imageDrawY, imageDrawDiameter, imageDrawDiameter, null);
        } else {
        	// Fallback: if image not loaded, draw a colored circle (optional)
            g.setColor(Color.GRAY);
            g.fillOval(x - (int) radius, y - (int) radius, (int) (radius * 2), (int) (radius * 2));
        }
        g.setColor(Color.BLACK);
        g.drawOval(x - (int) radius, y - (int) radius, (int) (radius * 2), (int) (radius * 2));
    }

    // Checks if this ball is colliding with another ball
    public boolean intersects(Ball other) {
        double dx = x - other.x;
        double dy = y - other.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (this.radius + other.radius);
    }
}