package mines.mines;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.security.SecureRandom;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;


public class Board extends JPanel {

    private static final int NUM_IMAGES = 13;
    private static final int CELL_SIZE = 15;

    private static final int COVER_FOR_CELL = 10;
    private static final int MARK_FOR_CELL = 10;
    private static final int EMPTY_CELL = 0;
    private static final int MINE_CELL = 9;
    private static final int COVERED_MINE_CELL = MINE_CELL + COVER_FOR_CELL;
    private static final int MARKED_MINE_CELL = COVERED_MINE_CELL + MARK_FOR_CELL;

    private static final int DRAW_MINE = 9;
    private static final int DRAW_COVER = 10;
    private static final int DRAW_MARK = 11;
    private static final int DRAW_WRONG_MARK = 12;

    private int[] field;
    private boolean inGame;
    private int minesLeft;
    private transient Image[] img;
    private int mines = 40;
    private int rows = 16;
    private int cols = 16;
    private int allCells;
    private JLabel statusbar;


    public Board(JLabel statusbar) {

        this.statusbar = statusbar;

        img = new Image[NUM_IMAGES];

        for (int i = 0; i < NUM_IMAGES; i++) {
            img[i] = (new ImageIcon("C:\\Users\\win\\Downloads\\JeuMines-20251011T192442Z-1-001\\JeuMines\\images\\" + i + ".gif")).getImage();
        }

        setDoubleBuffered(true);

        addMouseListener(new MinesAdapter());
        newGame();
    }

    private void initializeGameState() {
        inGame = true;
        minesLeft = mines;
        allCells = rows * cols;
        field = new int[allCells];
    }
    private void initializeField() {
        for (int i = 0; i < allCells; i++) {
            field[i] = COVER_FOR_CELL;
        }
    }
    private void updateStatusBar() {
        statusbar.setText(Integer.toString(minesLeft));
    }
    private void placeMines(SecureRandom random) {
        int placed = 0;
        while (placed < mines) {
            int position = random.nextInt(allCells);

            if (position >= allCells || field[position] == COVERED_MINE_CELL) continue;

            field[position] = COVERED_MINE_CELL;
            placed++;

            updateAdjacentCells(position);
        }
    }
    private void updateAdjacentCells(int position) {
        int currentCol = position % cols;
        int[] offsets = {
                -cols - 1, -1, +cols - 1,
                -cols, +cols,
                -cols + 1, +cols + 1, +1
        };

        for (int offset : offsets) {
            int cell = position + offset;

            boolean skipLeft = (offset == -cols - 1 || offset == -1 || offset == +cols - 1) && currentCol == 0;
            boolean skipRight = (offset == -cols + 1 || offset == +cols + 1 || offset == +1) && currentCol == cols - 1;

            if (skipLeft || skipRight) continue;

            if (cell >= 0 && cell < allCells && field[cell] != COVERED_MINE_CELL) {
                field[cell] += 1;
            }
        }
    }
    public void newGame() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);

        initializeGameState();
        initializeField();
        updateStatusBar();
        placeMines(random);
    }


    public void findEmptyCells(int j) {
        int currentCol = j % cols;
        int[] offsets = {
                -cols - 1, -1, +cols - 1,
                -cols, +cols,
                -cols + 1, +cols + 1, +1
        };

        for (int offset : offsets) {
            int cell = j + offset;

            boolean skipLeft = (offset == -cols - 1 || offset == -1 || offset == +cols - 1) && currentCol == 0;
            boolean skipRight = (offset == -cols + 1 || offset == +cols + 1 || offset == +1) && currentCol == cols - 1;

            if (skipLeft || skipRight) {
                continue;
            }

            if (cell >= 0 && cell < allCells && field[cell] > MINE_CELL) {
                field[cell] -= COVER_FOR_CELL;
                if (field[cell] == EMPTY_CELL) {
                    findEmptyCells(cell);
                }
            }
        }
    }

    private int resolveCellDrawing(int cell) {
        if (!inGame) {
            if (cell == COVERED_MINE_CELL) return DRAW_MINE;
            if (cell == MARKED_MINE_CELL) return DRAW_MARK;
            if (cell > COVERED_MINE_CELL) return DRAW_WRONG_MARK;
            if (cell > MINE_CELL) return DRAW_COVER;
        } else {
            if (cell > COVERED_MINE_CELL) return DRAW_MARK;
            if (cell > MINE_CELL) return DRAW_COVER;
        }
        return cell;
    }
    private void updateGameStatus(int uncover) {
        if (uncover == 0 && inGame) {
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame) {
            statusbar.setText("Game lost");
        }
    }
    @Override
    public void paint(Graphics g) {
        int cell;
        int uncover = 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                int index = (i * cols) + j;
                cell = field[index];

                if (inGame && cell == MINE_CELL) {
                    inGame = false;
                }

                cell = resolveCellDrawing(cell);
                if (cell == DRAW_COVER && inGame) {
                    uncover++;
                }

                g.drawImage(img[cell], (j * CELL_SIZE), (i * CELL_SIZE), this);
            }
        }

        updateGameStatus(uncover);
    }


    class MinesAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cCol = x / CELL_SIZE;
            int cRow = y / CELL_SIZE;

            if (!inGame) {
                newGame();
                repaint();
                return;
            }

            if (x >= cols * CELL_SIZE || y >= rows * CELL_SIZE) return;

            boolean rep = false;
            int index = (cRow * cols) + cCol;

            if (e.getButton() == MouseEvent.BUTTON3) {
                rep = handleRightClick(index);
            } else {
                rep = handleLeftClick(index);
            }

            if (rep) repaint();
        }
        private boolean handleRightClick(int index) {
            if (field[index] <= MINE_CELL) return false;

            if (field[index] <= COVERED_MINE_CELL) {
                if (minesLeft > 0) {
                    field[index] += MARK_FOR_CELL;
                    minesLeft--;
                    statusbar.setText(Integer.toString(minesLeft));
                } else {
                    statusbar.setText("No marks left");
                }
            } else {
                field[index] -= MARK_FOR_CELL;
                minesLeft++;
                statusbar.setText(Integer.toString(minesLeft));
            }

            return true;
        }
        private boolean handleLeftClick(int index) {
            if (field[index] > COVERED_MINE_CELL) return false;

            if (field[index] > MINE_CELL && field[index] < MARKED_MINE_CELL) {
                field[index] -= COVER_FOR_CELL;

                if (field[index] == MINE_CELL) {
                    inGame = false;
                } else if (field[index] == EMPTY_CELL) {
                    findEmptyCells(index);
                }

                return true;
            }

            return false;
        }
    }
}