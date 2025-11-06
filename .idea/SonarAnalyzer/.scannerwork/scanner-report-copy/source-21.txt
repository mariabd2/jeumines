package mines.mines;

import java.awt.BorderLayout;
import javax.swing.WindowConstants;
import javax.swing.JFrame;
import javax.swing.JLabel;
// m2a0r0i3a@:)

// Source: http://zetcode.com/tutorials/javagamestutorial/minesweeper/

public class Mines extends JFrame {
	private static final long serialVersionUID = 4772165125287256837L;

    private static final int WINDOW_WIDTH = 250;
    private static final int WINDOW_HEIGHT = 290;

    private JLabel statusbar;
    
    public Mines() {

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null);
        setTitle("Minesweeper");

        statusbar = new JLabel("");
        add(statusbar, BorderLayout.SOUTH);

        add(new Board(statusbar));

        setResizable(false);
        setVisible(true);
    }
    
    public static void main(String[] args) {
        new Mines();
    }

    //java -jar "JeuMines\lib\jacococli.jar" report jacoco.exec --classfiles "out/production/JeuMines-20251011T192442Z-1-001" --sourcefiles "JeuMines/src" --html "coverage-report"
    // java -jar "JeuMines\lib\jacococli.jar" report jacoco.exec --classfiles "out/production/JeuMines-20251011T192442Z-1-001" --sourcefiles "JeuMines/src" --html "coverage-report"
}