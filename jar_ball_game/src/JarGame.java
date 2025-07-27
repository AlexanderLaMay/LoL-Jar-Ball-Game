// JarGame.java
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// Main class to set up the game window
public class JarGame extends JFrame {

    private GamePanel gamePanel;

    public JarGame() {
        setTitle("Jar Drop Game");
        setSize(400, 700); // Increased height for better visibility of jar and balls
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        gamePanel = new GamePanel();
        add(gamePanel);

        setResizable(false); // For simplicity, keep window size fixed
        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JarGame::new); // Run the game on the Event Dispatch Thread
    }
}