package mines.mines;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;
    private JLabel statusbar;

    @BeforeEach
    public void setUp() {
        statusbar = new JLabel();
        board = new Board(statusbar);
    }

    @Test
    void testNewGameInitializesField() {
        board.newGame();
        int[] field = getPrivateField();
        assertEquals(256, field.length); // 16x16 grid
    }

    @Test
    void testMineCountAfterNewGame() {
        board.newGame();
        int mineCount = countMines();
        assertEquals(40, mineCount);
    }

    @Test
    void testFindEmptyCellsDoesNotCrash() {
        board.newGame();
        assertDoesNotThrow(() -> board.findEmptyCells(0));
    }

    // Helper to access private field via reflection
    private int[] getPrivateField() {
        try {
            var fieldField = Board.class.getDeclaredField("field");
            fieldField.setAccessible(true);
            return (int[]) fieldField.get(board);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int countMines() {
        int[] field = getPrivateField();
        int count = 0;
        for (int cell : field) {
            if (cell == 19) count++; // COVERED_MINE_CELL = 9 + 10
        }
        return count;
    }
}