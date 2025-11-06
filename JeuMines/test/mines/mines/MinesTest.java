package mines.mines;

import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class MinesTest {

    @Test
    void testWindowTitleAndSize() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Mines mines = new Mines();
            assertEquals("Minesweeper", mines.getTitle());
            assertEquals(250, mines.getWidth());
            assertEquals(290, mines.getHeight());
            mines.dispose();
        });
    }

    @Test
    void testWindowIsVisibleAndNotResizable() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Mines mines = new Mines();
            assertTrue(mines.isVisible());
            assertFalse(mines.isResizable());
            mines.dispose();
        });
    }

    @Test
    void testStatusBarIsPresent() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Mines mines = new Mines();
            Component[] components = mines.getContentPane().getComponents();
            boolean found = false;
            for (Component comp : components) {
                if (comp instanceof JLabel) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Status bar should be present");
            mines.dispose();
        });
    }

    @Test
    void testBoardComponentIsPresent() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Mines mines = new Mines();
            Component[] components = mines.getContentPane().getComponents();
            boolean found = false;
            for (Component comp : components) {
                if (comp instanceof Board) {
                    found = true;
                    break;
                }
            }
            assertTrue(found, "Board component should be present");
            mines.dispose();
        });
    }

    @Test
    void testMainMethodCoverage() throws Exception {
        SwingUtilities.invokeAndWait(() -> Mines.main(new String[]{}));
    }
    @Test
    void testMinesConstructorCoverage() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Mines mines = new Mines();
            assertEquals("Minesweeper", mines.getTitle());
            assertFalse(mines.isResizable());
            assertTrue(mines.isVisible());
            mines.dispose();
        });
    }

}