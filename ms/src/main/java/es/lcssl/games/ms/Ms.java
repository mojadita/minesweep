/*
 * Copyright (c) 2023, lcu
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package es.lcssl.games.ms;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import static java.lang.String.format;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Minesweeper game panel.
 *
 * @author lcu
 */
public class Ms extends JPanel {

    public static final byte MINE = (byte) 0x80;
    public static final byte ALREADY_OPENED = 0x40;
    public static final byte MINES_MASK = 0x0f;
    public static final double DEFAULT_PROB = 0.12;

    public static final int DEFAULT_ROWS = 32;
    public static final int DEFAULT_COLS = 32;

    public static final String PROPERTY_CELLS_TO_GO = "cellsToGo";
    public static final String PROPERTY_MINES = "mines";

    public static final Insets DEFAULT_BUTTON_INSETS
            = new Insets(1, 1, 1, 1);

    public static final Color bg[] = {
        Color.WHITE, Color.CYAN, Color.GREEN, Color.YELLOW,
        Color.ORANGE, Color.PINK, Color.MAGENTA, Color.RED,
        Color.DARK_GRAY};

    private final int rows, cols;

    private final byte[][] cells;

    private final JButton[][] pbs;
    private final Random rnd = new Random();
    private int mines;
    private int cellsToGo;

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private static ImageIcon flagged;
    private static ImageIcon exploded;

    static {
        try {
            ClassLoader cl = Ms.class.getClassLoader();
            flagged = new ImageIcon(
                    ImageIO.read(cl.getResourceAsStream("flagged.png")));
            exploded = new ImageIcon(
                    ImageIO.read(cl.getResourceAsStream("exploded.png")));
        } catch (IOException | IllegalArgumentException ex) {
            Logger.getLogger(Ms.class.getName()).log(Level.SEVERE,
                    "Cannot read resources 'flagged' &| 'exploded'", ex);
        }
    }

    private void init(double prob) {

        int N = rows * cols, n = (int) (N * prob + 0.5);

        int array[] = new int[N];
        for (int i = 0; i < N; i++) {
            array[i] = i;
            /* to select unique random cells (non-repeating) */
        }

        for (int i = 0; i < n; i++) {
            int cell = i + rnd.nextInt(N--);
            if (cell != i) {
                int temp = array[i];
                array[i] = array[cell];
                array[cell] = temp;
            }
            cell = array[i];

            int r = cell / cols, c = cell % cols;
            cells[r][c] = MINE;
            /* set mine */

 /* mark neighbor cells */
            markSurroundingCells(r - 1, c - 1);
            markSurroundingCells(r - 1, c);
            markSurroundingCells(r - 1, c + 1);
            markSurroundingCells(r, c - 1);
            markSurroundingCells(r, c + 1);
            markSurroundingCells(r + 1, c - 1);
            markSurroundingCells(r + 1, c);
            markSurroundingCells(r + 1, c + 1);
        }

        /* create the pushbuttons */
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                final JButton b = new JButton();
                b.setSize(16, 16);
                b.setMargin(DEFAULT_BUTTON_INSETS);
                b.setAction(new PushButtonAction(r, c, b));
                b.setText(null);
                b.setIcon(null);
                add(b);
                pbs[r][c] = b;
            }
        }

        mines = n;
        cellsToGo = N - n;
    }

    public Ms(int rows, int cols, double prob) {
        super(new GridLayout(rows, cols));

        this.rows = rows;
        this.cols = cols;
        cells = new byte[rows][cols];
        pbs = new JButton[rows][cols];
        init(prob);
    }

    private boolean isInBoard(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private class PushButtonAction extends AbstractAction {

        public final int r, c;
        public final JButton b;

        public PushButtonAction(int r, int c, JButton b) {
            this.r = r;
            this.c = c;
            this.b = b;
        }

        private void uncoverNeighbor(
                final int r, final int c, final ActionEvent e) {

            if (isInBoard(r, c) && (cells[r][c] & ALREADY_OPENED) == 0) {
                /* call the actionPerformed of the target cell with the event
                 * of the calling code */
                pbs[r][c].getAction().actionPerformed(e);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            System.out.println(
                    format("Button@(%d, %d) pressed\n",
                            r, c));

            b.setBorderPainted(false);
            if (cells[r][c] == MINE) {
                /* hit a mine */
                b.setIcon(exploded);
                System.out.println("BOUMMMMMM!!!");
//                Ms.this.setEnabled(false);
//                for (int r = 0; r < rows; r++)
//                    for (int c = 0; c < cols; c++)
//                        pbs[r][c].setEnabled(false);
            } else if ((cells[r][c] & ALREADY_OPENED) == 0) { /* not already open */
                /* not a mine and covered, uncover */
                b.setBackground(bg[cells[r][c] & MINES_MASK]);
                int old = cellsToGo--;
                pcs.firePropertyChange(PROPERTY_CELLS_TO_GO,
                        old, cellsToGo);
                cells[r][c] |= ALREADY_OPENED;
                if ((cells[r][c] & MINES_MASK) > 0) {
                    b.setText(format(
                            "%d", cells[r][c] & MINES_MASK));
                    if ((cells[r][c] & MINES_MASK) >= 6)
                        b.setForeground(Color.WHITE);
                } else {
                    EventQueue.invokeLater(() -> {
                        uncoverNeighbor(r - 1, c - 1, e);
                        uncoverNeighbor(r - 1, c, e);
                        uncoverNeighbor(r - 1, c + 1, e);
                        uncoverNeighbor(r, c - 1, e);
                        uncoverNeighbor(r, c + 1, e);
                        uncoverNeighbor(r + 1, c - 1, e);
                        uncoverNeighbor(r + 1, c, e);
                        uncoverNeighbor(r + 1, c + 1, e);
                    });
                }
            }
        }
    }

    public void addPropertyChangeListener(String property, PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(property, pcl);
    }

    public void removePropertyChangeListener(String property, PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(property, pcl);
    }

    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(pcl);
    }

    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        pcs.removePropertyChangeListener(pcl);
    }

    public Ms(int rows, int cols) {
        this(rows, cols, DEFAULT_PROB);
    }

    public Ms() {
        this(DEFAULT_ROWS, DEFAULT_COLS, DEFAULT_PROB);
    }

    private void markSurroundingCells(int r, int c) {
        if (isInBoard(r, c) && cells[r][c] != MINE) {
            cells[r][c]++;
        }
    }

    protected void line(StringBuilder sb, int cols) {
        sb.append("+");
        for (int c = 0; c < cols; c++) {
            sb.append("--");
        }
        sb.append("-+\n");
    }

    public int getMines() {
        return mines;
    }

    public int getCellsToGo() {
        return cellsToGo;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        line(sb, cols);
        for (int r = 0; r < rows; r++) {
            sb.append("|");
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] == MINE) {
                    sb.append(" @");
                } else if ((cells[r][c] & MINES_MASK) == 0) {
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
}
