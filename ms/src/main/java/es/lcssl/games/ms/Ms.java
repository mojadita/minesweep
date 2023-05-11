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
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

import static java.lang.String.format;

/**
 * Minesweeper game panel. This has to be divided in two classes:
 *
 * <ul>
 * <li>The model.
 * <li>The Swing widget.
 * </ul>
 *
 * @author lcu
 */
public class Ms extends JPanel {

    public static final byte MINE = (byte) 0x80;
    public static final byte ALREADY_OPENED = 0x40;
    public static final byte MARK_MASK = 0x20;
    public static final byte MINES_MASK = 0x0f;
    public static final double DEFAULT_PROB = 0.12;
    public static final int DEFAULT_ROWS = 32;
    public static final int DEFAULT_COLS = 32;
    public static final String PROPERTY_CELLS_TO_GO = "cellsToGo";
    public static final String PROPERTY_MINES = "mines";
    public static final String PROPERTY_LOST = "lost";
    public static final String PROPERTY_WON = "won";
    public static final int PREFERRED_SIZE = 28;

    public static final Insets DEFAULT_BUTTON_INSETS =
            new Insets( 1, 1, 1, 1 );

    public static final Color bg[] = {
        Color.WHITE, Color.CYAN, Color.GREEN, Color.YELLOW,
        Color.ORANGE, Color.PINK, Color.MAGENTA, Color.RED,
        Color.DARK_GRAY };

    private static ImageIcon flagged;
    private static ImageIcon exploded;

    static {
        /* class initialization code, load the icons */
        try {
            ClassLoader cl = Ms.class.getClassLoader();
            flagged = new ImageIcon(
                    ImageIO.read( cl.getResourceAsStream(
                            "flagged.png" ) ) );
            exploded = new ImageIcon(
                    ImageIO.read( cl.getResourceAsStream(
                            "exploded.png" ) ) );
        } catch ( IOException | IllegalArgumentException ex ) {
            Logger.getLogger( Ms.class.getName() )
                    .log( Level.SEVERE,
                          "Cannot read resources 'flagged' &| 'exploded'",
                          ex );
        }
    }

    private final int rows, cols;
    private byte[][] cells;
    private final JButton[][] pushbuttonActionSupport;
    private final Random rnd = new Random();
    private int minesToMark;
    private int cellsToGo;
    private boolean lost = false;
    private boolean won = false;
    private double probability = DEFAULT_PROB;
    private final PropertyChangeSupport propertyChangeSupport =
            new PropertyChangeSupport( this );

    public void init() {

        int N = rows * cols,
                n = (int) (N * probability + 0.5),
                array[] = new int[N];

        cells = new byte[rows][cols];

        for ( int i = 0; i < N; i++ ) {
            array[i] = i;
            /* to select unique random cells (non-repeating) */
        }

        for ( int i = 0; i < n; i++ ) {
            /* cell is a random, non selected yet cell */
            int cell = i + rnd.nextInt( N-- );
            if ( cell != i ) {
                /* exchange it with actual to exclude from next
                 * selections */
                int temp = array[i];
                array[i] = array[cell];
                array[cell] = temp;
            }
            cell = array[i];

            /* extract row and column */
            int r = cell / cols,
                    c = cell % cols;

            /* set mine */
            cells[r][c] = MINE;

            /* mark neighbor cells */
            incrementSurroundingCellAt( r - 1, c - 1 );
            incrementSurroundingCellAt( r - 1, c );
            incrementSurroundingCellAt( r - 1, c + 1 );
            incrementSurroundingCellAt( r, c - 1 );
            incrementSurroundingCellAt( r, c + 1 );
            incrementSurroundingCellAt( r + 1, c - 1 );
            incrementSurroundingCellAt( r + 1, c );
            incrementSurroundingCellAt( r + 1, c + 1 );
        }
        /* init the pushbuttons */
        for ( int r = 0; r < rows; r++ ) {
            for ( int c = 0; c < cols; c++ ) {
                JButton b = pushbuttonActionSupport[r][c];
                /* if we don't still have the pushbutton, create it */
                if ( b == null ) {
                    b = pushbuttonActionSupport[r][c] = new JButton();
                    /* and add to this panel */
                    add( b );
                    /* create and sect the action associated to the button */
                    b.setAction( new PushButtonAction( r, c, b ) );
                    b.setMargin( DEFAULT_BUTTON_INSETS );
                }
                /* set/reset the button */
                b.setText( null );
                b.setIcon( null );
                b.setBackground( null );
                b.setForeground( null );
                b.setBorderPainted( true );
            }
        }
        pushbuttonActionSupport[0][0].setPreferredSize(
                new Dimension( PREFERRED_SIZE, PREFERRED_SIZE ) );

        minesToMark = n;
        cellsToGo = N;
        lost = false;
        won = false;
        firePropertyChange( PROPERTY_CELLS_TO_GO, 0, cellsToGo );
        firePropertyChange( PROPERTY_MINES, 0, minesToMark );
    }

    public Ms( int rows, int cols, double prob ) {
        super( new GridLayout( rows, cols ) );

        this.rows = rows;
        this.cols = cols;
        probability = prob;
        cells = new byte[rows][cols];
        pushbuttonActionSupport = new JButton[rows][cols];
        init();
    }

    public Ms( int rows, int cols ) {
        this( rows, cols, DEFAULT_PROB );
    }

    public Ms() {
        this( DEFAULT_ROWS, DEFAULT_COLS, DEFAULT_PROB );
    }

    private boolean isCellInBoard( int r, int c ) {
        return r >= 0 && r < rows && c >= 0 && c < cols;
    }

    private class PushButtonAction extends AbstractAction {

        public final int r, c;
        public final JButton b;

        public PushButtonAction( int r, int c, JButton b ) {
            this.r = r;
            this.c = c;
            this.b = b;
        }

        private void uncover(
                final int r, final int c,
                final ActionEvent e ) {

            if ( isCellInBoard( r, c )
                    && (cells[r][c] & ALREADY_OPENED) == 0 ) {
                /* call the actionPerformed of the target cell with the event
                 * of the calling code */
                pushbuttonActionSupport[r][c]
                        .getAction().actionPerformed( e );
            }
        }

        private boolean isMarked( int r, int c ) {
            return isCellInBoard( r, c ) && (cells[r][c] & MARK_MASK) != 0;
        }

        @Override
        public void actionPerformed( ActionEvent e ) {

            if ( lost || won ) {
                /* finished game */
                System.out.println( "already finished, reinit game" );
                return;
            }

            byte cell_value = cells[r][c];

            if ( (cell_value & ALREADY_OPENED) != 0 ) {
                /* already open */
                if ( (cell_value & MINES_MASK) > 0 ) {
                    int marked = 0;
                    if ( isMarked( r - 1, c - 1 ) ) marked++;
                    if ( isMarked( r - 1, c ) ) marked++;
                    if ( isMarked( r - 1, c + 1 ) ) marked++;
                    if ( isMarked( r, c - 1 ) ) marked++;
                    if ( isMarked( r, c + 1 ) ) marked++;
                    if ( isMarked( r + 1, c - 1 ) ) marked++;
                    if ( isMarked( r + 1, c ) ) marked++;
                    if ( isMarked( r + 1, c + 1 ) ) marked++;
                    if ( marked == (cell_value & MINES_MASK) ) {
                        EventQueue.invokeLater( () -> {
                            /* change the ActionEvent into an appropiate one */
                            ActionEvent e2 = new ActionEvent(
                                    this,
                                    ActionEvent.ACTION_PERFORMED,
                                    "openFlagged" );
                            if ( !isMarked( r - 1, c - 1 ) )
                                uncover( r - 1, c - 1, e2 );
                            if ( !isMarked( r - 1, c ) )
                                uncover( r - 1, c, e2 );
                            if ( !isMarked( r - 1, c + 1 ) )
                                uncover( r - 1, c + 1, e2 );
                            if ( !isMarked( r, c - 1 ) )
                                uncover( r, c - 1, e2 );
                            if ( !isMarked( r, c + 1 ) )
                                uncover( r, c + 1, e2 );
                            if ( !isMarked( r + 1, c - 1 ) )
                                uncover( r + 1, c - 1, e2 );
                            if ( !isMarked( r + 1, c ) )
                                uncover( r + 1, c, e2 );
                            if ( !isMarked( r + 1, c + 1 ) )
                                uncover( r + 1, c + 1, e2 );
                        } );
                    }
                    return;
                }
                System.out.println( "already opened" );
                return;
            }

            if ( (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0 ) {
                /* switch marked mine */
                cells[r][c] ^= MARK_MASK;
                pushbuttonActionSupport[r][c].setIcon(
                        (cell_value & MARK_MASK) == 0
                                ? flagged
                                : null );
                int old_val = minesToMark;
                minesToMark += (cell_value & MARK_MASK) == 0
                        ? -1
                        : +1;
                propertyChangeSupport
                        .firePropertyChange(
                                PROPERTY_MINES,
                                old_val, minesToMark );
                return;
            }

            b.setBorderPainted( false );

            if ( cell_value == MINE ) {
                /* We hit a mine */
                b.setIcon( exploded );
                b.setBackground( Color.red );
                lost = true;
                propertyChangeSupport
                        .firePropertyChange( PROPERTY_LOST,
                                             false, lost );
                return;
            }

            /* not already open
             * not a mine, and covered, uncover */
            int surrounding = cell_value & MINES_MASK;
            b.setBackground( bg[surrounding] );
            if ( surrounding >= 6 ) {
                b.setForeground( Color.WHITE );
            }
            if ( surrounding > 0 ) {
                b.setText( format( "%d", surrounding ) );
            }

            int old = cellsToGo--;

            propertyChangeSupport
                    .firePropertyChange( PROPERTY_CELLS_TO_GO,
                                         old, cellsToGo );
            if ( cellsToGo == 0 && !won ) {
                won = true;
                propertyChangeSupport
                        .firePropertyChange( PROPERTY_WON,
                                             false, true );
                return;
            }

            cells[r][c] |= ALREADY_OPENED;
            if ( surrounding == 0 ) {
                /* surrounding == 0, uncover all surrounding */
                EventQueue.invokeLater( () -> {
                    uncover( r - 1, c - 1, e );
                    uncover( r - 1, c, e );
                    uncover( r - 1, c + 1, e );
                    uncover( r, c - 1, e );
                    uncover( r, c + 1, e );
                    uncover( r + 1, c - 1, e );
                    uncover( r + 1, c, e );
                    uncover( r + 1, c + 1, e );
                } );
            }
        }
    }

    @Override
    public void addPropertyChangeListener(
            String property,
            PropertyChangeListener listener ) {
        propertyChangeSupport.addPropertyChangeListener(
                property, listener );
    }

    @Override
    public void removePropertyChangeListener(
            String property,
            PropertyChangeListener listener ) {
        propertyChangeSupport.removePropertyChangeListener(
                property, listener );
    }

    private void incrementSurroundingCellAt( int r, int c ) {
        if ( isCellInBoard( r, c ) && cells[r][c] != MINE ) {
            cells[r][c]++;
        }
    }

    private void line( StringBuilder sb, int cols ) {
        sb.append( "+" );
        for ( int c = 0; c < cols; c++ ) {
            sb.append( "--" );
        }
        sb.append( "-+\n" );
    }

    public int getMinesToMark() {
        return minesToMark;
    }

    public int getCellsToGo() {
        return cellsToGo;
    }

    public boolean isLost() {
        return lost;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        line( sb, cols );
        for ( int r = 0; r < rows; r++ ) {
            sb.append( "|" );
            for ( int c = 0; c < cols; c++ ) {
                if ( cells[r][c] == MINE ) {
                    sb.append( " @" );
                } else if ( (cells[r][c] & MINES_MASK) == 0 ) {
                    sb.append( "  " );
                } else {
                    sb.append( format( " %d",
                                       cells[r][c] & MINES_MASK ) );
                }
            }
            sb.append( " |\n" );
        }
        line( sb, cols );
        return sb.toString();
    }
}
