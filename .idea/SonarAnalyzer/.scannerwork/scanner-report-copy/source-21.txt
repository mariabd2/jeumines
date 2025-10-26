package mines.mines;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
// m2a0r0i3a@:)

// Source: http://zetcode.com/tutorials/javagamestutorial/minesweeper/

public class Mines extends JFrame {
	private static final long serialVersionUID = 4772165125287256837L;
	
	private final int width = 250;
    private final int hight = 290;

    private JLabel statusbar;
    
    public Mines() {

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, hight);
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
}