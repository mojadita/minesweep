/*
 * License file not set.
 * 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */
package es.lcssl.games.ms;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.JPanel;
import static java.lang.String.format;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author lcu
 */
public class Ms extends JPanel {

    public static final byte MINE = (byte) 0x80;
    public static final byte MINES_MASK = 0x0f;
    public static final double DEFAULT_PROB = 0.12;
    public static final int DEFAULT_ROWS = 10;
    public static final int DEFAULT_COLS = 10;

    public static final Color bg[] = {
        Color.WHITE, Color.GREEN, Color.CYAN, Color.YELLOW, Color.ORANGE,
        Color.PINK, Color.MAGENTA, Color.RED, Color.DARK_GRAY};

    private final int rows, cols;

    private final byte[][] cells;

    public Ms(int rows, int cols, double prob) {
        super(new GridLayout(rows, cols));

        this.rows = rows;
        this.cols = cols;
        cells = new byte[rows][cols];
        int N = rows * cols, n = (int) (N * prob + 0.5);
        Random rnd = new Random();
        for (int i = 0; i < n; i++) {
            int cell = i + rnd.nextInt(N--);
            int r = cell / cols, c = cell % cols;
            cells[r][c] = MINE;
            markSurroundingCells(r - 1, c - 1);
            markSurroundingCells(r - 1, c);
            markSurroundingCells(r - 1, c + 1);
            markSurroundingCells(r, c - 1);
            markSurroundingCells(r, c + 1);
            markSurroundingCells(r + 1, c - 1);
            markSurroundingCells(r + 1, c);
            markSurroundingCells(r + 1, c + 1);
        }
        EventQueue.invokeLater(() -> {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    JButton b = new JButton();
                    b.setAction(new PushButtonAction(r, c, b));
                    add(b);
                }
            }
        });
    }

    private class PushButtonAction extends AbstractAction {

        public final int r, c;
        public final JButton b;

        public PushButtonAction(int r, int c, JButton b) {
            this.r = r;
            this.c = c;
            this.b = b;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            EventQueue.invokeLater(() -> {
                System.out.println(format("Button@(%d, %d) pressed\n", r, c));
                b.setBorderPainted(false);
                b.setOpaque(false);
                if (cells[r][c] == MINE) {
                    b.setBackground(Color.BLACK);
                    System.out.println("BOUMMMMMM!!!");
                } else {
                    b.setBackground(bg[cells[r][c] & MINES_MASK]);
                }
                if ((cells[r][c] & MINES_MASK) > 0) {
                    b.setText(format(
                            "%d", cells[r][c] & MINES_MASK));
                    b.setPreferredSize(new Dimension(32, 32));
                }

            });
        }

    }

    public Ms(int rows, int cols) {
        this(rows, cols, DEFAULT_PROB);
    }

    public Ms() {
        this(DEFAULT_ROWS, DEFAULT_COLS, DEFAULT_PROB);
    }

    private void markSurroundingCells(int r, int c) {
        if (r >= 0 && r < rows
                && c >= 0 && c < cols
                && cells[r][c] != MINE) {
            cells[r][c]++;
        }
    }

    void line(StringBuilder sb, int cols) {
        sb.append("+");
        for (int c = 0; c < cols; c++) {
            sb.append("--");
        }
        sb.append("-+\n");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        line(sb, cols);
        for (int r = 0; r < rows; r++) {
            sb.append("|");
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] == MINE) {
                    sb.append(" @");
                } else if (cells[r][c] == 0) {
                    sb.append("  ");
                } else {
                    sb.append(format(" %d",
                            cells[r][c] & MINES_MASK));
                }
            }
            sb.append(" |\n");
        }
        line(sb, cols);
        return sb.toString();
    }

    public static void main(String[] args) {
        Ms board = new Ms();
        JFrame frame = new JFrame(Ms.class.getSimpleName());
        frame.setContentPane(board);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(320, 320);
        frame.setVisible(true);
        System.out.println(board);
    }
}
