/*
 * Copyright (c) 2023, lcu.  Dedicated to Maela
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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import static java.text.MessageFormat.format;
import static java.util.ResourceBundle.getBundle;

/**
 * Main class providing a frame and using the {@link MineSweeper} widget to
 * play
 * mine hunting.
 *
 * @author lcu
 */
public class Main {

    private static final Logger LOG
            = Logger.getLogger( Main.class.getName() );

    private static final ResourceBundle intl
            = getBundle( Main.class.getName() );

    /**
     * Main program, it creates a JFrame with some interesting widgets to
     * obtain
     * information about the {@link MineSweeper} board, and how the values are
     * calculated.
     *
     * @param args parameterized options {@code --rows}, {@code --cols} and
     *             {@code --prob} are used to indicate grid rows number, grid columns
     *             number
     *             and mine probability (as a number between 0 and 100) for each grid.
     *             This
     *             last parameter is used to generate the number of cells to populate with
     *             a
     *             mine, and that number of mines is actually generated (so once
     *             calculated,
     *             exactly that number of mines is actually placed on the board)
     *             Parameters
     *             default to {@link MineSweeper#DEFAULT_ROWS} for the rows number; to
     *             {@link MineSweeper#DEFAULT_COLS} for the columns number, and to
     *             {@link MineSweeper#DEFAULT_PROB} for the number of mines calculation.
     *
     */
    public static void main( String[] args ) {
        int rows = MineSweeper.DEFAULT_ROWS, cols = MineSweeper.DEFAULT_COLS;
        double prob = MineSweeper.DEFAULT_PROB;

        /* process program arguments */
        for ( int i = 0; i < args.length; i++ ) {
            switch ( args[ i ] ) {
                case "--rows":
                    rows = Integer.parseInt( args[ ++i ] );
                    break;
                case "--cols":
                    cols = Integer.parseInt( args[ ++i ] );
                    break;
                case "--prob":
                    prob = Double.parseDouble( args[ ++i ] );
                    break;
                default:
                    LOG.config( format(
                            intl.getString( "INVALID_PARAMETER" ),
                            i, args[ i ] ) );
                    break;
            }
        }

        JFrame frame = new JFrame( intl.getString( "TITLE" ) );

        // this is the MineSweeper board
        MineSweeper board = new MineSweeper( rows, cols, prob );
        JScrollPane sp = new JScrollPane( board );
        JMenuBar mb = new JMenuBar();
        frame.setJMenuBar( mb );
        JMenu file_menu = new JMenu( intl.getString( "FILE" ) );

        /* These {@link ValueField}s show the cells to go and
         * mines to go values.
         */
        ValueField places_to_go = new ValueField(
                intl.getString( "CELLS" ),
                intl.getString( "FORMAT_CELLS" ),
                board.getCellsToGo() );
        board.addPropertyChangeListener(
                MineSweeper.PROPERTY_CELLS_TO_GO,
                places_to_go );

        ValueField mines_to_guard = new ValueField(
                intl.getString( "MINES" ),
                intl.getString( "FORMAT_MINES" ),
                board.getMinesToMark() );

        board.addPropertyChangeListener(
                MineSweeper.PROPERTY_MINES,
                mines_to_guard );

        Chronograph chrono = new Chronograph();
        ValueField time = new ValueField(
                intl.getString( "TIME" ),
                intl.getString( "FORMAT_TIME" ),
                chrono );

        chrono.addValueChangeListener(
                Chronograph.PROPERTY_TIMESTAMP, time );


        /* add a reset menu option */
        file_menu.add( new AbstractAction( intl.getString( "RE-INIT" ) ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                EventQueue.invokeLater( () -> {
                    board.init();
                    places_to_go.propertyChange( new PropertyChangeEvent(
                            board,
                            MineSweeper.PROPERTY_CELLS_TO_GO,
                            0, board.getCellsToGo() ) );
                    mines_to_guard.propertyChange( new PropertyChangeEvent(
                            board,
                            MineSweeper.PROPERTY_MINES,
                            0, board.getMinesToMark() ) );
                    time.propertyChange(
                            new PropertyChangeEvent(
                                    chrono,
                                    Chronograph.PROPERTY_TIMESTAMP,
                                    Chronograph.toString( 0 ),
                                    Chronograph.toString( 0 ) ) );

                    chrono.reset();
                    board.addPropertyChangeListener(
                            MineSweeper.PROPERTY_CELLS_TO_GO,
                            new ChronoPropertyChangeListener(
                                    board,
                                    MineSweeper.PROPERTY_CELLS_TO_GO,
                                    chrono::start ) );
                    board.validate();
                } );
            }
        } );

        final HallOfFameComponent hall_of_fame = new HallOfFameComponent(board,
                intl.getString( "HALL_OF_FAME_BASE_DIR" ));

        file_menu.add( new AbstractAction( intl.getString( "HALL_OF_FAME" ) ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                hall_of_fame.setVisible( true);
            }
        } );

        /* Add a quit button */
        file_menu.add( new AbstractAction( intl.getString( "QUIT" ) ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                EventQueue.invokeLater( () -> {
                    System.exit( 0 );
                } );
            }
        } );

        /* build the menu */
        mb.add( file_menu );
        mb.add( new JSeparator( JSeparator.VERTICAL ) );
        mb.add( time );
        mb.add( new JSeparator( JSeparator.VERTICAL ) );
        mb.add( places_to_go );
        mb.add( mines_to_guard );
        frame.setContentPane( sp );
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.pack();
        frame.setVisible( true );

//        System.out.println(board);

        /* register a loser callback */
        board.addPropertyChangeListener(
                MineSweeper.PROPERTY_LOST,
                ev -> {
                    if ( (boolean) ev.getNewValue() ) {
                        chrono.stop();
                        String error_msg = intl.getString( "EXPLODED" );
                        JOptionPane.showMessageDialog(
                                frame,
                                error_msg,
                                intl.getString( "ERROR_MESSAGE" ),
                                JOptionPane.ERROR_MESSAGE );
                        LOG.info( error_msg );
                    }
                } );

        /* ... and a winner callback */
        board.addPropertyChangeListener(
                MineSweeper.PROPERTY_WON,
                ev -> {
                    chrono.stop();
                    String success_msg = intl.getString( "SUCCESS" );
                    JOptionPane.showMessageDialog(
                            frame,
                            success_msg,
                            intl.getString( "SUCCESS_MESSAGE" ),
                            JOptionPane.INFORMATION_MESSAGE );
                    LOG.info( success_msg );
                    hall_of_fame.getModel().addScore(
                            System.currentTimeMillis(),
                            chrono.getTimeMillis());
                } );

        /* ... chronograph set */
        board.addPropertyChangeListener(
                MineSweeper.PROPERTY_CELLS_TO_GO,
                new ChronoPropertyChangeListener(
                        board,
                        MineSweeper.PROPERTY_CELLS_TO_GO,
                        chrono::start ) );
    }

    private static class ChronoPropertyChangeListener
            implements PropertyChangeListener {

        private static final Logger LOG = Logger.getLogger(
                ChronoPropertyChangeListener.class.getSimpleName() );

        Runnable toDo;
        String propertyName;
        MineSweeper board;

        ChronoPropertyChangeListener(
                MineSweeper board, String property_name,
                Runnable to_do ) {
            toDo = to_do;
            propertyName = property_name;
            this.board = board;
        }

        @Override
        public void propertyChange( PropertyChangeEvent evt ) {
            LOG.fine( evt.toString() );
            toDo.run();
            board.removePropertyChangeListener(
                    propertyName, this );
        }
    }
}
