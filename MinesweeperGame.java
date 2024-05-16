
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.sound.sampled.*;
import java.io.File;
// @author Navya Reddy
public class MinesweeperGame extends JFrame {
    private static final int BEGINNER_SIZE_X = 6;
    private static final int BEGINNER_SIZE_Y = 9;
    private static final int BEGINNER_MINES = 11;

    private static final int INTERMEDIATE_SIZE_X = 12;
    private static final int INTERMEDIATE_SIZE_Y = 18;
    private static final int INTERMEDIATE_MINES = 36;

    private static final int ADVANCED_SIZE_X = 21;
    private static final int ADVANCED_SIZE_Y = 26;
    private static final int ADVANCED_MINES = 92;

    private int sizeX;
    private int sizeY;
    private int numMines;

    private JButton[][] board;
    private int[][] mineField;
    private boolean[][] revealed;
    private boolean[][] markedAsMine;

    private int secondsElapsed;
    private Timer timer;
    private JLabel timerLabel;

    private boolean gameInProgress;

    public MinesweeperGame() {
        chooseDifficultyLevel();
    }

    private void chooseDifficultyLevel() {
        Object[] options = { "Beginner", "Intermediate", "Advanced" };
        int choice = JOptionPane.showOptionDialog(this,
                "Choose difficulty level:",
                "Minesweeper",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 0:
                initializeGame(BEGINNER_SIZE_X, BEGINNER_SIZE_Y, BEGINNER_MINES);
                break;
            case 1:
                initializeGame(INTERMEDIATE_SIZE_X, INTERMEDIATE_SIZE_Y, INTERMEDIATE_MINES);
                break;
            case 2:
                initializeGame(ADVANCED_SIZE_X, ADVANCED_SIZE_Y, ADVANCED_MINES);
                break;
            default:
                // Default to beginner if the dialog is closed
                initializeGame(BEGINNER_SIZE_X, BEGINNER_SIZE_Y, BEGINNER_MINES);
        }

        initializeGUI();
    }
    // @author Navya Reddy
	// @param integer values of X  Y and number of mines
    private void initializeGame(int sizeX, int sizeY, int numMines) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.numMines = numMines;

        board = new JButton[sizeX][sizeY];
        mineField = new int[sizeX][sizeY];
        revealed = new boolean[sizeX][sizeY];
        markedAsMine = new boolean[sizeX][sizeY];

        secondsElapsed = 0;
        timer = new Timer(1000, e -> updateTimer());
        timer.start();

        generateMines();
        calculateAdjacentMines();

        gameInProgress = true;
    }
    // @author Navya Reddy
    private void initializeGUI() {
        setTitle("Minesweeper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create and add the timer label
        timerLabel = new JLabel("Time: 0 seconds");
        timerLabel.setHorizontalAlignment(JLabel.CENTER);
        add(timerLabel, BorderLayout.NORTH);

        JPanel boardPanel = new JPanel(new GridLayout(sizeX, sizeY));

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                board[i][j] = new JButton();
                int x = i;
                int y = j;

                board[i][j].addActionListener(e -> handleLeftClick(x, y));
                board[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            if (e.getClickCount() == 1) {
                                handleRightClick(x, y);
                            } else if (e.getClickCount() == 2) {
                                handleDoubleRightClick(x, y);
                            }
                        }
                    }
                });

                boardPanel.add(board[i][j]);
            }
        }

        add(boardPanel, BorderLayout.CENTER);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
    // @author Navya Reddy
    private void generateMines() {
        Random random = new Random();

        for (int i = 0; i < numMines; i++) {
            int x, y;
            do {
                x = random.nextInt(sizeX);
                y = random.nextInt(sizeY);
            } while (mineField[x][y] == -1);

            mineField[x][y] = -1;
        }
    }
    // @author Navya Reddy
    private void calculateAdjacentMines() {
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (mineField[i][j] == -1) {
                    continue;
                }

                int count = 0;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int newX = i + dx;
                        int newY = j + dy;

                        if (newX >= 0 && newX < sizeX && newY >= 0 && newY < sizeY && mineField[newX][newY] == -1) {
                            count++;
                        }
                    }
                }

                mineField[i][j] = count;
            }
        }
    }
    // @author Navya Reddy
    private void updateTimer() {
        if (!gameInProgress) {
            timer.stop();
            return;
        }

        secondsElapsed++;

        if ((sizeX == BEGINNER_SIZE_X && sizeY == BEGINNER_SIZE_Y && secondsElapsed >= 60)
                || (sizeX == INTERMEDIATE_SIZE_X && sizeY == INTERMEDIATE_SIZE_Y && secondsElapsed >= 180)
                || (sizeX == ADVANCED_SIZE_X && sizeY == ADVANCED_SIZE_Y && secondsElapsed >= 660)) {
            explodeMines();
            gameOver(false);
        }

        // Update the timer label
        timerLabel.setText("Time: " + secondsElapsed + " seconds");
    }
    // @author Navya Reddy
	// @param integer value of x and y
    private void handleLeftClick(int x, int y) {
        if (!gameInProgress || revealed[x][y] || markedAsMine[x][y]) {
            return;
        }

        revealed[x][y] = true;
        board[x][y].setEnabled(false);

        if (mineField[x][y] == -1) {
            explodeMines();
            gameOver(true);
        } else if (mineField[x][y] == 0) {
            revealEmptyCells(x, y);
        } else {
            updateButton(x, y);
        }

        checkGameWin();
    }
    // @author Navya Reddy 
	// @param integer value of x and y
    private void handleRightClick(int x, int y) {
        if (!gameInProgress || revealed[x][y]) {
            return;
        }

        markedAsMine[x][y] = !markedAsMine[x][y];
        updateButton(x, y);

        // Check for incorrectly marked mines
        if (mineField[x][y] != -1 && markedAsMine[x][y]) {
            board[x][y].setText("X");
            board[x][y].setForeground(Color.RED);
        }

        checkGameWin();
    }
    // @author Navya Reddy
	// @param integer value of x and y
    private void handleDoubleRightClick(int x, int y) {
        if (revealed[x][y] && mineField[x][y] > 0) {
            int markedCount = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    int newX = x + dx;
                    int newY = y + dy;

                    if (newX >= 0 && newX < sizeX && newY >= 0 && newY < sizeY && markedAsMine[newX][newY]) {
                        markedCount++;
                    }
                }
            }

            if (markedCount == mineField[x][y]) {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        int newX = x + dx;
                        int newY = y + dy;

                        if (newX >= 0 && newX < sizeX && newY >= 0 && newY < sizeY && !revealed[newX][newY]
                                && !markedAsMine[newX][newY]) {
                            handleLeftClick(newX, newY);
                        }
                    }
                }
            }
        }
    }
    // @author Navya Reddy
	// @param integer value of x and y
    private void revealEmptyCells(int x, int y) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int newX = x + dx;
                int newY = y + dy;

                if (newX >= 0 && newX < sizeX && newY >= 0 && newY < sizeY && !revealed[newX][newY]) {
                    revealed[newX][newY] = true;
                    board[newX][newY].setEnabled(false);

                    if (mineField[newX][newY] == 0) {
                        revealEmptyCells(newX, newY);
                    } else {
                        updateButton(newX, newY);
                    }
                }
            }
        }
    }
    // @author Navya Reddy
	// @param integer value of x and y
    private void updateButton(int x, int y) {
        if (revealed[x][y]) {
            if (mineField[x][y] == -1) {
                board[x][y].setText("X");
                board[x][y].setBackground(Color.RED);
            } else if (mineField[x][y] > 0) {
                board[x][y].setText(Integer.toString(mineField[x][y]));
            }
        } else if (markedAsMine[x][y]) {
            board[x][y].setText("M");
        } else {
            board[x][y].setText("");
        }
    }
    // @author Navya Reddy
    private void checkGameWin() {
        boolean win = true;

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (!revealed[i][j] && mineField[i][j] != -1) {
                    win = false;
                    break;
                }
            }
        }

        if (win) {
            gameInProgress = false;
            timer.stop();
            JOptionPane.showMessageDialog(this, "Congratulations! You won!", "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
    // @author Navya Reddy
    private void explodeMines() {
        // Implement mine explosion animation and sound
        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                if (mineField[i][j] == -1 && !revealed[i][j] && !markedAsMine[i][j]) {
                    board[i][j].setText("X");
                    board[i][j].setBackground(Color.RED);
                }
            }
        }

        // Play explosion sound (you may need to adjust the file path)
        try {
            Clip clip = AudioSystem.getClip();
            AudioInputStream audioInputStream = AudioSystem
                    .getAudioInputStream(new File("explosion.wav").getAbsoluteFile());
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // @author Navya Reddy
	// @param boolean explosion of game
    private void gameOver(boolean explosion) {
        gameInProgress = false;
        timer.stop();

        if (explosion) {
            JOptionPane.showMessageDialog(this, "Game Over! Explosion!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Game Over! Time limit exceeded!", "Game Over",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        resetGame();
    }
    // @author Navya Reddy
    private void resetGame() {
        secondsElapsed = 0;
        initializeGame(sizeX, sizeY, numMines);

        for (int i = 0; i < sizeX; i++) {
            for (int j = 0; j < sizeY; j++) {
                board[i][j].setText("");
                board[i][j].setEnabled(true);
                board[i][j].setBackground(null);
            }
        }
    }
    // @author Navya Reddy
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MinesweeperGame());
    }
}