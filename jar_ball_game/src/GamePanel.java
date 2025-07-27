// GamePanel.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// The main game panel where all drawing and logic occurs
class GamePanel extends JPanel implements ActionListener, MouseListener, MouseMotionListener {

    // Game state variables
    private List<Ball> balls;
    private Ball currentDroppingBall;
    private Timer gameLoopTimer;
    private boolean gameOver;
    private long score;

    // Jar dimensions
    private final int JAR_WIDTH = 300;
    private final int JAR_HEIGHT = 500;
    private final int JAR_X;
    private final int JAR_Y;
    private final int JAR_TOP_Y; // Y-coordinate where game ends if balls reach it

    // Physics constants
    private final double GRAVITY = 5.0; // Pixels per frame squared
    private final double BOUNCE_FACTOR = 0.7; // How much velocity is retained after bounce
    private final double FRICTION = 0.95;

    // Ball generation
    private Random random;
    private final int MAX_INITIAL_BALL_LEVEL = 3; // Max level for randomly generated balls
    private final int MAX_BALL_LEVEL_FOR_IMAGES = 6;

    //Max ball level
    private final int MAX_MERGE_LEVEL = 6;
    
    //Delay for game over so you don't instantly lose
    private final long OUT_OF_BOUNDS_DELAY_MS= 4000;
    
    //Map to store ball images by level
    private HashMap<Integer, BufferedImage> ballImages;
    
    public GamePanel() {
        balls = new ArrayList<>();
        random = new Random();
        gameOver = false;
        score = 0;

        //Load ball images
        loadBallImages();
        
        // Calculate jar position to center it
        JAR_X = (400 - JAR_WIDTH) / 2;
        JAR_Y = 600 - JAR_HEIGHT; // Jar bottom at Y=600, top at Y=100
        JAR_TOP_Y = JAR_Y + 20; // A little buffer from the actual jar top line

        addMouseListener(this);
        addMouseMotionListener(this);

        // Initialize the first ball to drop
        spawnNewDroppingBall();

        // Game loop timer (updates every 16ms, approx 60 FPS)
        gameLoopTimer = new Timer(16, this);
        gameLoopTimer.start();
    }
    
    //Method to load all ball images
    private void loadBallImages() {
        ballImages = new HashMap<>();
        try {
            // Load images for initial ball levels
            for (int i = 0; i <= MAX_BALL_LEVEL_FOR_IMAGES; i++) {
                String imagePath = "/resources/ball_level_" + i + ".png"; // Path relative to classpath
                BufferedImage img = ImageIO.read(getClass().getResource(imagePath));
                if (img != null) {
                    ballImages.put(i, img);
                } else {
                    System.err.println("Warning: Could not load image: " + imagePath);
                }
            }
            // For levels beyond MAX_BALL_LEVEL_FOR_IMAGES, you might use a generic image
            // or dynamically generate one (e.g., a colored circle with text)
            // For now, if an image isn't found, the Ball class will draw a gray circle.

        } catch (IOException e) {
            System.err.println("Error loading ball images: " + e.getMessage());
            e.printStackTrace();
            // Handle error, e.g., use default colors or exit
        }
    }

    // Spawns a new ball at the top, ready to be moved and dropped
    private void spawnNewDroppingBall() {
        int initialLevel = random.nextInt(MAX_INITIAL_BALL_LEVEL + 1); // 0 to MAX_INITIAL_BALL_LEVEL
        BufferedImage ballImage = ballImages.get(initialLevel);
        currentDroppingBall = new Ball(getWidth() / 2, 50, initialLevel, ballImage); // Start above the jar
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw background
        g.setColor(new Color(240, 248, 255)); // Alice Blue
        g.fillRect(0, 0, getWidth(), getHeight());

        // Draw the jar
        g.setColor(new Color(173, 216, 230, 150)); // Light Blue with transparency
        g.fillRoundRect(JAR_X, JAR_Y, JAR_WIDTH, JAR_HEIGHT, 30, 30);
        g.setColor(new Color(70, 130, 180)); // Steel Blue border
        g2d.setStroke(new BasicStroke(5));
        g.drawRoundRect(JAR_X, JAR_Y, JAR_WIDTH, JAR_HEIGHT, 30, 30);

        // Draw jar opening
        g.setColor(new Color(JAR_TOP_Y, 216, 230, 200));
        g.fillRoundRect(JAR_X - 10, JAR_Y - 20, JAR_WIDTH + 20, 40, 20, 20);
        g.setColor(new Color(70, 130, 180));
        g.drawRoundRect(JAR_X - 10, JAR_Y - 20, JAR_WIDTH + 20, 40, 20, 20);


        // Draw all existing balls
        for (Ball ball : balls) {
            ball.draw(g);
        }

        // Draw the current dropping ball (if not null and game not over)
        if (currentDroppingBall != null && !gameOver) {
            currentDroppingBall.draw(g);
        }

        // Draw score
        g.setColor(Color.BLACK);
        g.setFont(new Font("Inter", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 30);

        // Draw game over message
        if (gameOver) {
            g.setColor(new Color(0, 0, 0, 180)); // Semi-transparent black
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Inter", Font.BOLD, 40));
            String gameOverText = "GAME OVER!";
            String scoreText = "Final Score: " + score;
            FontMetrics fm = g.getFontMetrics();
            int textWidth = fm.stringWidth(gameOverText);
            int textHeight = fm.getHeight();
            g.drawString(gameOverText, (getWidth() - textWidth) / 2, getHeight() / 2 - textHeight / 2);

            g.setFont(new Font("Inter", Font.PLAIN, 25));
            textWidth = fm.stringWidth(scoreText);
            g.drawString(scoreText, (getWidth() - textWidth) / 2, getHeight() / 2 + textHeight / 2 + 10);

            // Restart button
            g.setColor(new Color(50, 200, 50)); // Green
            g.fillRoundRect(getWidth() / 2 - 80, getHeight() / 2 + 70, 160, 50, 20, 20);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Inter", Font.BOLD, 20));
            String restartText = "Restart";
            textWidth = fm.stringWidth(restartText);
            g.drawString(restartText, (getWidth() - textWidth) / 2, getHeight() / 2 + 70 + 30);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updateGame();
            repaint();
        }
    }

    // Updates the game state (physics, collisions, merging)
    private void updateGame() {

        // Update current dropping ball's position if it's falling
        if (currentDroppingBall != null && currentDroppingBall.y > currentDroppingBall.radius + 50) {
            currentDroppingBall.y += GRAVITY;
            currentDroppingBall.y += currentDroppingBall.vy;
            applyBoundaryConstraints(currentDroppingBall);
        }
        
        //Apply gravity and update position for all active balls
        for (Ball ball : balls) {
        	ball.vy += GRAVITY;
        	ball.vx *= FRICTION;
        	ball.vy *= FRICTION;
        	
        	ball.x += ball.vx;
        	ball.y += ball.vy;
        	
        	applyBoundaryConstraints(ball);
        	
        	//Wall collisions
        	//Left wall
        	if (ball.x - ball.radius < JAR_X) {
        		ball.x = JAR_X + (int) ball.radius;
        		ball.vx *= -BOUNCE_FACTOR;
        	}
        	//Right wall
        	if (ball.x + ball.radius > JAR_X + JAR_WIDTH) {
        		ball.x = JAR_X + JAR_WIDTH - (int) ball.radius;
        		ball.vx *= -BOUNCE_FACTOR;
        	}
        	//Jar bottom
        	if (ball.y + ball.radius > JAR_Y + JAR_HEIGHT) {
        		ball.y = JAR_Y + JAR_HEIGHT - (int) ball.radius;
        		ball.vy *= -BOUNCE_FACTOR;
        		//reduce horizontal velocity on floor bounce
        		ball.vx *= 0.9;
        	}
        }

        // Collision detection and resolution
        for (int i = 0; i < balls.size(); i++) {
            Ball b1 = balls.get(i);

            // Collision with jar bottom
            if (b1.y + b1.radius > JAR_Y + JAR_HEIGHT) {
                b1.y = JAR_Y + JAR_HEIGHT - (int) b1.radius;
            }

            // Collision with jar sides
            if (b1.x - b1.radius < JAR_X) {
                b1.x = JAR_X + (int) b1.radius;
            }
            if (b1.x + b1.radius > JAR_X + JAR_WIDTH) {
                b1.x = JAR_X + JAR_WIDTH - (int) b1.radius;
            }
        }

        // Collision detection and resolution (iterative for stability)
        // Perform multiple iterations to resolve overlapping balls that might be pushed into new overlaps
        for (int iter = 0; iter < 5; iter++) { // 5 iterations usually sufficient
            boolean mergedThisIteration = false;
            for (int i = 0; i < balls.size(); i++) {
                Ball b1 = balls.get(i);
                // Apply boundary constraints to b1 before checking collisions with other balls
                applyBoundaryConstraints(b1); // Ensure b1 is within bounds

                for (int j = i + 1; j < balls.size(); j++) {
                    Ball b2 = balls.get(j);
                    // Apply boundary constraints to b2 as well
                    applyBoundaryConstraints(b2); // Ensure b2 is within bounds

                    double dx = b1.x - b2.x;
                    double dy = b1.y - b2.y;
                    double distance = Math.sqrt(dx * dx + dy * dy);
                    double minDistance = b1.radius + b2.radius;

                    if (distance < minDistance) {
                        // Collision detected!

                        // Check if merging would exceed the MAX_MERGE_LEVEL
                        if (b1.level == b2.level && (b1.level + 1) > MAX_MERGE_LEVEL) {
                            // If merging would exceed max level, just resolve collision and don't merge
                            // This block ensures they bounce off each other without growing further.
                            // The rest of the collision response logic below will handle the bounce.
                        } else if (b1.level == b2.level) {
                            // If same level and within merge limit, merge them
                            mergeBalls(b1, b2);
                            mergedThisIteration = true;
                            // After merge, the list 'balls' changes, so we need to restart the iteration
                            // This is why we use 'return' here, and the outer loop will handle the re-check.
                            return;
                        }

                        // Resolve overlap (push balls apart) - applies to both merging and non-merging collisions
                        double overlap = minDistance - distance;
                        if (distance == 0) { // Handle perfect overlap (balls at same spot)
                            dx = Math.random() - 0.5; // Give a slight random push
                            dy = Math.random() - 0.5;
                            distance = Math.sqrt(dx * dx + dy * dy);
                            if (distance == 0) distance = 1; // Avoid division by zero
                        }
                        double nx = dx / distance; // Normal vector x
                        double ny = dy / distance; // Normal vector y

                        double moveAmount = overlap / (b1.mass + b2.mass); // Distribute movement based on mass
                        b1.x += nx * moveAmount * b2.mass;
                        b1.y += ny * moveAmount * b2.mass;
                        b2.x -= nx * moveAmount * b1.mass;
                        b2.y -= ny * moveAmount * b1.mass;

                        // Apply collision response (adjust velocities)
                        double relativeVelocityX = b1.vx - b2.vx;
                        double relativeVelocityY = b1.vy - b2.vy;
                        double dotProduct = relativeVelocityX * nx + relativeVelocityY * ny;

                        // If balls are moving away from each other, no need for impulse
                        if (dotProduct > 0) continue;

                        double impulse = -(1 + BOUNCE_FACTOR) * dotProduct / (1 / b1.mass + 1 / b2.mass);

                        b1.vx += impulse * nx / b1.mass;
                        b1.vy += impulse * ny / b1.mass;
                        b2.vx -= impulse * nx / b2.mass;
                        b2.vy -= impulse * ny / b2.mass;

                        // After resolving collision, re-apply boundary constraints to ensure they don't go out of bounds
                        applyBoundaryConstraints(b1);
                        applyBoundaryConstraints(b2);
                    }
                }
            }
                if (mergedThisIteration) {
                    // If a merge happened, the list changed, so re-run collision detection from start
                    // This 'return' effectively restarts the updateGame() cycle for the next frame,
                    // ensuring all new collisions are handled correctly.
                    return;
                }
        }
        
        for (Ball ball : balls) {
        	applyBoundaryConstraints(ball);
        }

        for (Ball ball : balls) {
            // Define "out of play" as being too high OR horizontally outside the jar
            boolean isOutOfPlay = (ball.y - ball.radius < JAR_TOP_Y) ||
                                  (ball.x + ball.radius < JAR_X) ||
                                  (ball.x - ball.radius > JAR_X + JAR_WIDTH);

            if (isOutOfPlay) {
                if (ball.outOfBoundsTimerStart == 0L) {
                    // Ball just went out of bounds, start the timer
                    ball.outOfBoundsTimerStart = System.currentTimeMillis();
                } else {
                    // Ball has been out of bounds, check if delay has passed
                    if (System.currentTimeMillis() - ball.outOfBoundsTimerStart > OUT_OF_BOUNDS_DELAY_MS) {
                        gameOver = true;
                        gameLoopTimer.stop();
                        break; // Exit loop as game is over
                    }
                }
            } else {
                // Ball is back in bounds, reset its timer
                ball.outOfBoundsTimerStart = 0L;
            }
        }
    }
    
    //Applies boundary constraints to a single ball
    private void applyBoundaryConstraints(Ball ball) {
    	 // Left wall
        if (ball.x - ball.radius < JAR_X) {
            ball.x = JAR_X + (int) ball.radius;
            ball.vx *= -BOUNCE_FACTOR;
        }
        // Right wall
        if (ball.x + ball.radius > JAR_X + JAR_WIDTH) {
            ball.x = JAR_X + JAR_WIDTH - (int) ball.radius;
            ball.vx *= -BOUNCE_FACTOR;
        }
        // Bottom wall (jar bottom)
        if (ball.y + ball.radius > JAR_Y + JAR_HEIGHT) {
            ball.y = JAR_Y + JAR_HEIGHT - (int) ball.radius;
            ball.vy *= -BOUNCE_FACTOR;
            // Reduce horizontal velocity slightly on floor bounce
            ball.vx *= 0.9;
        }
    }

    // Merges two balls into a larger one
    private void mergeBalls(Ball b1, Ball b2) {
        // Calculate new ball position (midpoint)
        int newX = (b1.x + b2.x) / 2;
        int newY = (b1.y + b2.y) / 2;

        // Create new ball with increased level
        int newLevel = b1.level + 1;
        BufferedImage newBallImage = ballImages.get(newLevel);
        Ball newBall = new Ball(newX, newY, newLevel, newBallImage);

        // Remove old balls and add new one
        balls.remove(b1);
        balls.remove(b2);
        balls.add(newBall);

        // Update score
        score += newBall.value; // Add value of the new, larger ball
    }

    // --- Mouse Listeners ---
    @Override
    public void mouseClicked(MouseEvent e) {
        if (gameOver) {
            // Check if restart button was clicked
            int buttonX = getWidth() / 2 - 80;
            int buttonY = getHeight() / 2 + 70;
            if (e.getX() >= buttonX && e.getX() <= buttonX + 160 &&
                e.getY() >= buttonY && e.getY() <= buttonY + 50) {
                restartGame();
            }
            return;
        }

        if (currentDroppingBall != null) {
            // Drop the current ball by adding it to the active balls list
            currentDroppingBall.y = 50; // Ensure it starts falling from the top
            balls.add(currentDroppingBall);
            currentDroppingBall = null; // No ball currently being controlled
            spawnNewDroppingBall(); // Prepare the next ball
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    @Override
    public void mouseDragged(MouseEvent e) {}

    @Override
    public void mouseMoved(MouseEvent e) {
        if (!gameOver && currentDroppingBall != null) {
            // Move the current dropping ball horizontally with the mouse
            int newX = e.getX();
            // Constrain the ball within the jar's top opening
            newX = Math.max(JAR_X + (int) currentDroppingBall.radius, newX);
            newX = Math.min(JAR_X + JAR_WIDTH - (int) currentDroppingBall.radius, newX);
            currentDroppingBall.x = newX;
            repaint(); // Repaint to show the ball moving
        }
    }

    // Resets the game to its initial state
    private void restartGame() {
        balls.clear();
        score = 0;
        gameOver = false;
        spawnNewDroppingBall();
        gameLoopTimer.start();
        repaint();
    }
}