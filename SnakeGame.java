import javax.swing.*; 
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SnakeGame extends JFrame {
    private GamePanel gamePanel;
    
    public SnakeGame() {
        setTitle("Snake Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gamePanel = new GamePanel();
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);
        setResizable(false);
    }
    
    public static void main(String[] args) {
        try {
            // Set system look and feel for better UI
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new SnakeGame().setVisible(true);
        });
    }
}

class GamePanel extends JPanel implements ActionListener {
    static final int WIDTH = 600;
    static final int HEIGHT = 600;
    static final int UNIT_SIZE = 20;
    static final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    static final int DELAY = 100;
    
    final int[] x = new int[GAME_UNITS];
    final int[] y = new int[GAME_UNITS];
    int bodyParts = 6;
    int foodEaten = 0;
    int highScore = 0;
    int foodX;
    int foodY;
    char direction = 'R';
    boolean running = false;
    boolean gameStarted = false;
    Timer timer;
    Random random;
    
    // Colors for better UI
    private final Color BACKGROUND_COLOR = new Color(40, 44, 52);
    private final Color SNAKE_HEAD_COLOR = new Color(82, 183, 136);
    private final Color SNAKE_BODY_COLOR = new Color(98, 209, 150);
    private final Color FOOD_COLOR = new Color(225, 98, 89);
    private final Color GRID_COLOR = new Color(50, 54, 62);
    
    // Add trail effect
    private final java.util.List<Point> snakeTrail = new java.util.ArrayList<>();
    
    // For Game Over Effect
    private boolean gameOverAnimating = false;
    private float gameOverAlpha = 0f;

    GamePanel() {
        random = new Random();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(BACKGROUND_COLOR);
        setFocusable(true);
        addKeyListener(new MyKeyAdapter());
        showStartScreen();
    }
    
    public void showStartScreen() {
        gameStarted = false;
        running = false;
        repaint();
    }
    
    public void startGame() {
        gameStarted = true;
        running = true;
        bodyParts = 6;
        foodEaten = 0;
        direction = 'R';
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 100 - i * UNIT_SIZE;
            y[i] = 100;
        }
        newFood();
        if (timer != null) timer.stop();
        timer = new Timer(DELAY, this);
        timer.start();
        snakeTrail.clear();  // Clear the trail on restart
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        if (!gameStarted) {
            drawStartScreen(g2d);
        } else if (running) {
            drawGame(g2d);
        } else {
            drawGameOver(g2d);
        }
    }
    
    private void drawGrid(Graphics2D g) {
        g.setColor(GRID_COLOR);
        for (int i = 0; i < WIDTH/UNIT_SIZE; i++) {
            g.drawLine(i * UNIT_SIZE, 0, i * UNIT_SIZE, HEIGHT);
            g.drawLine(0, i * UNIT_SIZE, WIDTH, i * UNIT_SIZE);
        }
    }
    
    private void drawStartScreen(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "SNAKE GAME";
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(title, (WIDTH - metrics.stringWidth(title))/2, HEIGHT/3);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String start = "Press SPACE to Start";
        metrics = getFontMetrics(g.getFont());
        g.drawString(start, (WIDTH - metrics.stringWidth(start))/2, HEIGHT/2);
        
        String controls = "Use Arrow Keys to Control";
        g.drawString(controls, (WIDTH - metrics.stringWidth(controls))/2, HEIGHT/2 + 40);
    }
    
    private void drawGame(Graphics2D g) {
        drawGrid(g);
        
        // Draw food with gradient
        GradientPaint foodGradient = new GradientPaint(
            foodX, foodY, FOOD_COLOR,
            foodX + UNIT_SIZE, foodY + UNIT_SIZE, FOOD_COLOR.brighter()
        );
        g.setPaint(foodGradient);
        g.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);
        
        // Draw snake
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                g.setColor(SNAKE_HEAD_COLOR);
                g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 10, 10);
            } else {
                g.setColor(SNAKE_BODY_COLOR);
                g.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, 5, 5);
            }
        }
        
        // Draw snake trail
        g.setColor(new Color(98, 209, 150, 100));  // Semi-transparent for fading effect
        for (Point trail : snakeTrail) {
            g.fillRoundRect(trail.x, trail.y, UNIT_SIZE, UNIT_SIZE, 5, 5);
        }

        // Draw score
        drawScore(g);
    }
    
    private void drawScore(Graphics2D g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String score = "Score: " + foodEaten;
        String high = "High Score: " + highScore;
        g.drawString(score, 10, 25);
        g.drawString(high, WIDTH - 150, 25);
    }
    
    private void drawGameOver(Graphics2D g) {
        drawGame(g);
        
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Game Over animation
        if (!gameOverAnimating) {
            gameOverAlpha = 1f;  // Reset alpha to full when the game is over
        }
        g.setColor(new Color(255, 0, 0, (int)(gameOverAlpha * 255))); // Red text
        g.setFont(new Font("Arial", Font.BOLD, 75));
        String gameOver = "Game Over";
        FontMetrics metrics = getFontMetrics(g.getFont());
        g.drawString(gameOver, (WIDTH - metrics.stringWidth(gameOver))/2, HEIGHT/2 - 50);
        
        // Score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        String score = "Score: " + foodEaten;
        metrics = getFontMetrics(g.getFont());
        g.drawString(score, (WIDTH - metrics.stringWidth(score))/2, HEIGHT/2 + 20);
        
        // Restart instruction
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String restart = "Press SPACE to Restart";
        metrics = getFontMetrics(g.getFont());
        g.drawString(restart, (WIDTH - metrics.stringWidth(restart))/2, HEIGHT/2 + 60);
    }
    
    public void newFood() {
        foodX = random.nextInt(WIDTH/UNIT_SIZE) * UNIT_SIZE;
        foodY = random.nextInt(HEIGHT/UNIT_SIZE) * UNIT_SIZE;
    }
    
    public void move() {
        // Save the head position before moving to create the trail effect
        snakeTrail.add(new Point(x[0], y[0]));
        if (snakeTrail.size() > 10) {
            snakeTrail.remove(0);  // Remove the oldest part of the trail
        }

        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i-1];
            y[i] = y[i-1];
        }
        
        switch(direction) {
            case 'U' -> y[0] = y[0] - UNIT_SIZE;
            case 'D' -> y[0] = y[0] + UNIT_SIZE;
            case 'L' -> x[0] = x[0] - UNIT_SIZE;
            case 'R' -> x[0] = x[0] + UNIT_SIZE;
        }
    }
    
    public void checkCollisions() {
        // Check if the snake hits the walls or itself
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
            gameOverAnimating = true;
        }
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
                gameOverAnimating = true;
            }
        }
        
        // Check if the snake eats the food
        if (x[0] == foodX && y[0] == foodY) {
            bodyParts++;
            foodEaten++;
            newFood();
        }
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (running) {
            move();
            checkCollisions();
            if (gameOverAnimating) {
                // Fade out the game over text slowly
                gameOverAlpha -= 0.05f;
                if (gameOverAlpha <= 0f) {
                    gameOverAnimating = false;  // Stop animation
                }
            }
        }
        repaint();
    }
    
    class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!gameStarted) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    startGame();
                }
            } else if (running) {
                if (e.getKeyCode() == KeyEvent.VK_LEFT && direction != 'R') {
                    direction = 'L';
                } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && direction != 'L') {
                    direction = 'R';
                } else if (e.getKeyCode() == KeyEvent.VK_UP && direction != 'D') {
                    direction = 'U';
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN && direction != 'U') {
                    direction = 'D';
                }
            } else {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    showStartScreen();
                }
            }
        }
    }
}
