import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class FlappyBird extends JPanel implements ActionListener, KeyListener {

    // Board settings
    int boardWidth = 360;
    int boardHeight = 640;

    // Bird settings
    int birdX = boardWidth / 8;
    int birdY = boardHeight / 2;
    int birdWidth = 34;
    int birdHeight = 24;
    int gravity = 1;
    int lift = 10;
    int velocity = 0;
    int velocityX = -4;

    // Pipe settings
    int pipeX = boardWidth;
    int pipeY = 0;
    int pipeWidth = 64;
    int pipeHeight = 512;

    // Game state variables
    boolean gameOver = false;
    boolean gameStarted = false;
    boolean gamePaused = false;
    int score = 0;

    // Images
    Image bgImage;
    Image birdImg;
    Image topPipeImg;
    Image bottomPipeImg;

    // Bird and Pipes
    Bird bird;
    ArrayList<Pipe> pipes;
    Random random = new Random();

    // Timers
    Timer gameLoop;
    Timer placePipesTimer;

    // Bird class
    class Bird {
        int x = birdX;
        int y = birdY;
        int width = birdWidth;
        int height = birdHeight;
        Image img;

        Bird(Image img) {
            this.img = img;
        }
    }

    // Pipe class
    class Pipe {
        int x = pipeX;
        int y = pipeY;
        int width = pipeWidth;
        int height = pipeHeight;
        boolean passed = false;
        Image img;

        Pipe(Image img) {
            this.img = img;
        }
    }

    // Constructor
    FlappyBird() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setFocusable(true);
        addKeyListener(this);

        // Load images
        bgImage = new ImageIcon(getClass().getResource("./flappybirdbg.png")).getImage();
        birdImg = new ImageIcon(getClass().getResource("./flappybird.png")).getImage();
        topPipeImg = new ImageIcon(getClass().getResource("./toppipe.png")).getImage();
        bottomPipeImg = new ImageIcon(getClass().getResource("./bottompipe.png")).getImage();

        bird = new Bird(birdImg);
        pipes = new ArrayList<>();

        placePipesTimer = new Timer(1500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                placePipes();
            }
        });

        gameLoop = new Timer(1000 / 60, this);
        gameLoop.start();
    }

    // Game mechanics
    public void placePipes() {
        int randomPipeY = (int) (pipeY - pipeHeight / 4 - Math.random() * (pipeHeight / 2));
        int openingSpace = boardHeight / 4;

        Pipe topPipe = new Pipe(topPipeImg);
        topPipe.y = randomPipeY;
        pipes.add(topPipe);

        Pipe bottomPipe = new Pipe(bottomPipeImg);
        bottomPipe.y = topPipe.y + pipeHeight + openingSpace;
        pipes.add(bottomPipe);
    }

    public void stopGame() {
        gameLoop.stop();
        placePipesTimer.stop();
    }

    public void restartGame() {
        bird.y = boardHeight / 2;
        pipes.clear();
        velocity = 0;
        score = 0;
        gameOver = false;
        gameStarted = false;
        gameLoop.start();
    }

    // Collision detection
    public boolean checkCollision(Pipe pipe) {
        Rectangle birdRect = new Rectangle(bird.x, bird.y, bird.width, bird.height);
        Rectangle pipeRect = new Rectangle(pipe.x, pipe.y, pipe.width, pipe.height);
        return birdRect.intersects(pipeRect);
    }

    // Rendering
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(bgImage, 0, 0, boardWidth, boardHeight, null);
        g.drawImage(bird.img, bird.x, bird.y, bird.width, bird.height, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 10, 30);

        for (Pipe pipe : pipes) {
            g.drawImage(pipe.img, pipe.x, pipe.y, pipe.width, pipe.height, null);
        }

        if (!gameStarted) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press Enter to Start", boardWidth / 4 - 20, boardHeight / 4);
            g.drawString("Press space to pause the game", 0, boardHeight / 2);
        }

        if (gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 32));
            g.drawString("Game Over", boardWidth / 2 - 80, boardHeight / 2);
            g.drawString("Score: " + score, boardWidth / 2 - 80, boardHeight / 3);
            g.drawString("Press Enter to Restart", boardWidth / 11 - 20, boardHeight / 2 + 50);
            score = 0;
        }

        if (gamePaused) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Game Paused", boardWidth / 4 - 20, boardHeight / 4);
        }
    }

    // Game loop
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gamePaused) {
            velocity += gravity;
            bird.y += velocity;
            bird.y = Math.max(bird.y, 0);
            bird.y = Math.min(bird.y, boardHeight - birdHeight);

            for (Pipe pipe : pipes) {
                pipe.x += velocityX;
                if (!pipe.passed && pipe.x + pipe.width < bird.x) {
                    pipe.passed = true;
                    score++;
                }

                if (checkCollision(pipe)) {
                    gameOver = true;
                    stopGame();
                }
            }

            if (bird.y >= boardHeight - birdHeight || bird.y <= 0) {
                gameOver = true;
                stopGame();
            }

            repaint();
        }
    }

    // Key events
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            velocity = -lift;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            velocity = lift;
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            if (gameOver) {
                restartGame();
            }
            gameStarted = true;
            placePipesTimer.start();
        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (gameStarted) {
                if (gamePaused) {
                    gamePaused = false;
                    gameLoop.start();
                    placePipesTimer.start();
                } else {
                    gamePaused = true;
                    gameLoop.stop();
                    placePipesTimer.stop();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN) {
            velocity = gravity;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used
    }
}
