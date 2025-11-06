package mines.mines;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JLabel;

import java.awt.event.MouseEvent;

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
    @Test
    void testInitializeFieldSetsAllCellsCovered() {
        board.newGame(); // triggers initializeField
        int[] field = getPrivateField();
        for (int cell : field) {
            assertTrue(cell >= 10); // COVER_FOR_CELL or higher
        }
    }

    @Test
    void testUpdateStatusBarReflectsMineCount() throws Exception {
        var method = Board.class.getDeclaredMethod("updateStatusBar");
        method.setAccessible(true);
        method.invoke(board);
        assertEquals("40", statusbar.getText());
    }

    @Test
    void testUpdateAdjacentCellsIncrementsNeighbors() throws Exception {
        board.newGame();
        int[] field = getPrivateField();
        int minePos = 85; // somewhere in the middle
        field[minePos] = 19; // COVERED_MINE_CELL

        var method = Board.class.getDeclaredMethod("updateAdjacentCells", int.class);
        method.setAccessible(true);
        method.invoke(board, minePos);

        int[] offsets = {-17, -1, 15, -16, 16, -15, 17, 1};
        for (int offset : offsets) {
            int neighbor = minePos + offset;
            if (neighbor >= 0 && neighbor < field.length && field[neighbor] != 19) {
                assertTrue(field[neighbor] >= 11); // incremented
            }
        }
    }

    @Test
    void testResolveCellDrawingReturnsExpectedValue() throws Exception {
        var method = Board.class.getDeclaredMethod("resolveCellDrawing", int.class);
        method.setAccessible(true);

        int drawMine = (int) method.invoke(board, 19); // COVERED_MINE_CELL
        assertEquals(9, drawMine); // DRAW_MINE
    }

    @Test
    void testUpdateGameStatusWinCondition() throws Exception {
        var method = Board.class.getDeclaredMethod("updateGameStatus", int.class);
        method.setAccessible(true);
        method.invoke(board, 0); // uncover == 0
        assertEquals("Game won", statusbar.getText());
    }
    @Test
    void testHandleRightClickMarksCell() throws Exception {
        board.newGame();
        int[] field = getPrivateField();
        int index = 100;
        field[index] = 10; // COVER_FOR_CELL

        var method = Board.class.getDeclaredMethod("handleRightClick", int.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(board, index);

        assertTrue(result);
        assertEquals(20, field[index]); // MARKED_MINE_CELL
    }
    @Test
    void testResolveCellDrawingVariants() throws Exception {
        var method = Board.class.getDeclaredMethod("resolveCellDrawing", int.class);
        method.setAccessible(true);

        int[] inputs = {19, 29, 20, 10, 9}; // COVERED_MINE_CELL, MARKED_MINE_CELL, >COVERED_MINE_CELL, >MINE_CELL, MINE_CELL
        for (int input : inputs) {
            method.invoke(board, input); // just invoke to trigger branches
        }
    }
    @Test
    void testGameLossUpdatesStatusBar() throws Exception {
        var method = Board.class.getDeclaredMethod("updateGameStatus", int.class);
        method.setAccessible(true);

        var inGameField = Board.class.getDeclaredField("inGame");
        inGameField.setAccessible(true);
        inGameField.set(board, false);

        method.invoke(board, 5);
        assertEquals("Game lost", statusbar.getText());
    }
    @Test
    void testHandleLeftClickOnMine() throws Exception {
        board.newGame();
        int[] field = getPrivateField();
        int index = 50;
        field[index] = 19; // COVERED_MINE_CELL

        var method = Board.class.getDeclaredMethod("handleLeftClick", int.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(board, index);

        assertFalse(result); // can't uncover marked mine
    }
    @Test
    void testRecursiveFindEmptyCells() {
        board.newGame();
        int[] field = getPrivateField();
        field[0] = 0; // EMPTY_CELL
        field[1] = 10; // COVER_FOR_CELL

        board.findEmptyCells(0);
        assertTrue(field[1] < 10); // should be uncovered
    }
    @Test
    void testMainMethodRunsWithoutError() {
        assertDoesNotThrow(() -> Mines.main(new String[]{}));
    }
    @Test
    void testHandleRightClickMarksAndUnmarksCell() throws Exception {
        board.newGame();
        int[] field = getPrivateField();
        int index = 50;
        field[index] = 10; // COVER_FOR_CELL

        var adapterClass = Class.forName("mines.mines.Board$MinesAdapter");
        var adapter = adapterClass.getDeclaredConstructor(board.getClass()).newInstance(board);

        var method = adapterClass.getDeclaredMethod("handleRightClick", int.class);
        method.setAccessible(true);

        boolean marked = (boolean) method.invoke(adapter, index);
        assertTrue(marked);
        assertEquals(20, field[index]); // MARKED_MINE_CELL

        boolean unmarked = (boolean) method.invoke(adapter, index);
        assertTrue(unmarked);
        assertEquals(10, field[index]); // back to COVER_FOR_CELL
    }

    @Test
    void testHandleLeftClickUncoversCell() throws Exception {
        board.newGame();
        int[] field = getPrivateField();
        int index = 60;
        field[index] = 11; // COVER_FOR_CELL + 1

        var adapterClass = Class.forName("mines.mines.Board$MinesAdapter");
        var adapter = adapterClass.getDeclaredConstructor(board.getClass()).newInstance(board);

        var method = adapterClass.getDeclaredMethod("handleLeftClick", int.class);
        method.setAccessible(true);

        boolean uncovered = (boolean) method.invoke(adapter, index);
        assertTrue(uncovered);
        assertEquals(1, field[index]); // uncovered value
    }

    @Test
    void testMousePressedTriggersNewGameOnLoss() throws Exception {
        board.newGame();
        var inGameField = Board.class.getDeclaredField("inGame");
        inGameField.setAccessible(true);
        inGameField.set(board, false); // simulate game over

        var adapterClass = Class.forName("mines.mines.Board$MinesAdapter");
        var adapter = adapterClass.getDeclaredConstructor(board.getClass()).newInstance(board);

        MouseEvent event = new MouseEvent(board, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1);
        var method = adapterClass.getDeclaredMethod("mousePressed", MouseEvent.class);
        method.setAccessible(true);
        method.invoke(adapter, event);

        assertTrue((boolean) inGameField.get(board)); // new game started
    }
    @Test
    void testResolveCellDrawingWhenNotInGame() throws Exception {
        var method = Board.class.getDeclaredMethod("resolveCellDrawing", int.class);
        method.setAccessible(true);

        var inGameField = Board.class.getDeclaredField("inGame");
        inGameField.setAccessible(true);
        inGameField.set(board, false); // simulate game over

        assertEquals(9, method.invoke(board, 19)); // COVERED_MINE_CELL → DRAW_MINE
        assertEquals(11, method.invoke(board, 29)); // MARKED_MINE_CELL → DRAW_MARK
        assertEquals(12, method.invoke(board, 30)); // > COVERED_MINE_CELL → DRAW_WRONG_MARK
        assertEquals(10, method.invoke(board, 10)); // > MINE_CELL → DRAW_COVER
    }
    @Test
    void testMousePressedOutsideBoardBounds() throws Exception {
        var adapterClass = Class.forName("mines.mines.Board$MinesAdapter");
        var adapter = adapterClass.getDeclaredConstructor(board.getClass()).newInstance(board);

        MouseEvent event = new MouseEvent(board, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 300, 300, 1, false, MouseEvent.BUTTON1);
        var method = adapterClass.getDeclaredMethod("mousePressed", MouseEvent.class);
        method.setAccessible(true);

        method.invoke(adapter, event); // should return early, no crash
    }

    @Test
    void testMousePressedTriggersNewGameWhenNotInGame() throws Exception {
        var inGameField = Board.class.getDeclaredField("inGame");
        inGameField.setAccessible(true);
        inGameField.set(board, false); // simulate game over

        var adapterClass = Class.forName("mines.mines.Board$MinesAdapter");
        var adapter = adapterClass.getDeclaredConstructor(board.getClass()).newInstance(board);

        MouseEvent event = new MouseEvent(board, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, 10, 10, 1, false, MouseEvent.BUTTON1);
        var method = adapterClass.getDeclaredMethod("mousePressed", MouseEvent.class);
        method.setAccessible(true);

        method.invoke(adapter, event); // should call newGame()
        assertTrue((boolean) inGameField.get(board)); // game restarted
    }

    @Test
    void testMousePressedHandlesRightClick() throws Exception {
        board.newGame();
        int[] field = getPrivateField();
        int index = 50;
        field[index] = 10; // COVER_FOR_CELL

        var adapterClass = Class.forName("mines.mines.Board$MinesAdapter");
        var adapter = adapterClass.getDeclaredConstructor(board.getClass()).newInstance(board);

        int x = (index % 16) * 15;
        int y = (index / 16) * 15;

        MouseEvent event = new MouseEvent(board, MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, x, y, 1, false, MouseEvent.BUTTON3);
        var method = adapterClass.getDeclaredMethod("mousePressed", MouseEvent.class);
        method.setAccessible(true);

        method.invoke(adapter, event);
        assertEquals(20, field[index]); // MARKED_MINE_CELL
    }
}