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
	private static final long serialVersionUID = 6195235521361212179L;

	private static final int numImages = 13;
    private static final int cellSize = 15;

    private static final int coverForCell = 10;
    private static final int markForCell = 10;
    private static final int emptyCell  = 0;
    private static final int mineCell  = 9;
    private static final int coveredMineCell  = mineCell + coverForCell;
    private static final int markedMineCell  = coveredMineCell + markForCell;

    private static final int drawMine  = 9;
    private static final int drawCover  = 10;
    private static final int drawMark  = 11;
    private static final int drawWrongMark  = 12;

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

        img = new Image[numImages];

        for (int i = 0; i < numImages; i++) {
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
            field[i] = coverForCell;
        }
    }
    private void updateStatusBar() {
        statusbar.setText(Integer.toString(minesLeft));
    }
    private void placeMines(SecureRandom random) {
        int placed = 0;
        while (placed < mines) {
            int position = random.nextInt(allCells);

            if (position >= allCells || field[position] == coveredMineCell) continue;

            field[position] = coveredMineCell;
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

            if (cell >= 0 && cell < allCells && field[cell] != coveredMineCell) {
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

            if (cell >= 0 && cell < allCells && field[cell] > mineCell) {
                field[cell] -= coverForCell;
                if (field[cell] == emptyCell) {
                    findEmptyCells(cell);
                }
            }
        }
    }

    private int resolveCellDrawing(int cell) {
        if (!inGame) {
            if (cell == coveredMineCell) return drawMine;
            if (cell == markedMineCell) return drawMark;
            if (cell > coveredMineCell) return drawWrongMark;
            if (cell > mineCell) return drawCover;
        } else {
            if (cell > coveredMineCell) return drawMark;
            if (cell > mineCell) return drawCover;
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

                if (inGame && cell == mineCell) {
                    inGame = false;
                }

                cell = resolveCellDrawing(cell);
                if (cell == drawCover && inGame) {
                    uncover++;
                }

                g.drawImage(img[cell], (j * cellSize), (i * cellSize), this);
            }
        }

        updateGameStatus(uncover);
    }


    class MinesAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            int x = e.getX();
            int y = e.getY();
            int cCol = x / cellSize;
            int cRow = y / cellSize;

            if (!inGame) {
                newGame();
                repaint();
                return;
            }

            if (x >= cols * cellSize || y >= rows * cellSize) return;

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
            if (field[index] <= mineCell) return false;

            if (field[index] <= coveredMineCell) {
                if (minesLeft > 0) {
                    field[index] += markForCell;
                    minesLeft--;
                    statusbar.setText(Integer.toString(minesLeft));
                } else {
                    statusbar.setText("No marks left");
                }
            } else {
                field[index] -= markForCell;
                minesLeft++;
                statusbar.setText(Integer.toString(minesLeft));
            }

            return true;
        }
        private boolean handleLeftClick(int index) {
            if (field[index] > coveredMineCell) return false;

            if (field[index] > mineCell && field[index] < markedMineCell) {
                field[index] -= coverForCell;

                if (field[index] == mineCell) {
                    inGame = false;
                } else if (field[index] == emptyCell) {
                    findEmptyCells(index);
                }

                return true;
            }

            return false;
        }
    }
}