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

import java.awt.BorderLayout;
import java.io.File;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;

import static java.text.MessageFormat.format;

/**
 *
 * @author lcu
 */
public class HallOfFameComponent extends JFrame {

    private static final Logger LOG
            = Logger.getLogger( HallOfFameComponent.class.getName() );

    private static final ResourceBundle INTL
            = ResourceBundle.getBundle( HallOfFameComponent.class.getName() );

    protected final HallOfFameModel model;
    protected final JList list;

    public HallOfFameComponent( MineSweeper ms, File base_dir ) {
        super( format( INTL.getString(
                "HALL_OF_FAME_DIALOG_NAME" ),
                ms.getRows(),
                ms.getCols(),
                ms.getMinesToMark() ) );
        JPanel panel = new JPanel( new BorderLayout() );
        JLabel label = new JLabel( format( INTL.getString(
                "HALL_OF_FAME_DIALOG_HEADER" ),
                ms.getRows(),
                ms.getCols(),
                ms.getMinesToMark() ) );
        label.setAlignmentX( 0.5F);
        panel.add( label, BorderLayout.NORTH );
        list = new JList<>( model = new HallOfFameModel(
                ms, base_dir ) );
        list.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        list.setDragEnabled( false );
        //list.setEnabled( false );
        list.setBorder( BorderFactory.createBevelBorder(
                BevelBorder.LOWERED ) );
        JScrollPane scroll_pane = new JScrollPane(list );
        panel.add( scroll_pane, BorderLayout.CENTER );
        add( panel );
        pack();
    }

    public HallOfFameModel getModel() {
        return model;
    }

    public JList getList() {
        return list;
    }

}
