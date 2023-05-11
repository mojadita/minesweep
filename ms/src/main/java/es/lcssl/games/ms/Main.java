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

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

/**
 *
 * @author lcu
 */
public class Main {

    public static void main(String[] args) {
        int rows = Ms.DEFAULT_ROWS, cols = Ms.DEFAULT_COLS;
        double prob = Ms.DEFAULT_PROB;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--rows":
                    rows = Integer.parseInt(args[++i]);
                    break;
                case "--cols":
                    cols = Integer.parseInt(args[++i]);
                    break;
                case "--prob":
                    prob = Double.parseDouble(args[++i]);
                    break;
                default:
                    System.err.println("Invalid parameter " + args[i]);
                    break;
            }
        }
        JFrame frame = new JFrame(Ms.class.getSimpleName());
        Ms board = new Ms(rows, cols, prob);
        JScrollPane sp = new JScrollPane(board);
        JMenuBar mb = new JMenuBar();
        frame.setJMenuBar(mb);
        JMenu file_menu = new JMenu("File");
        file_menu.add(new AbstractAction("Re-Init") {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(() -> {
                    board.init();
                    board.validate();
                });
            }
        });
        file_menu.add(new AbstractAction("Quit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                EventQueue.invokeLater(() -> {
                    System.exit(0);
                });
            }
        });
        ValueField places_to_go = new ValueField(
                "Cells", board.getCellsToGo());
        ValueField mines_to_guard = new ValueField(
                "Mines", board.getMinesToMark());
        board.addPropertyChangeListener(
                Ms.PROPERTY_CELLS_TO_GO, places_to_go);
        board.addPropertyChangeListener(
                Ms.PROPERTY_MINES, mines_to_guard);
        mb.add(file_menu);
        mb.add(places_to_go);
        mb.add(mines_to_guard);
        frame.setContentPane(sp);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        System.out.println(board);
        board.addPropertyChangeListener(Ms.PROPERTY_LOST, ev -> {
            if ((boolean) ev.getNewValue()) {
                JOptionPane.showMessageDialog(
                        frame,
                        "Oh!!  YOU EXPLODED\n",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        board.addPropertyChangeListener(
                Ms.PROPERTY_WON,
                ev -> {
                    JOptionPane.showMessageDialog(
                            frame,
                            "Oh well well!!! You were smart and "
                            + "you guessed all the mines",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                });
    }
}