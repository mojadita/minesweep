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

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import static java.text.MessageFormat.format;

/**
 *
 * @author lcu
 */
public class HallOfFame extends JFrame {

    private static final Logger LOG
            = Logger.getLogger( HallOfFame.class.getName() );

    private static final ResourceBundle INTL
            = ResourceBundle.getBundle( HallOfFame.class.getName() );

    private static final String WHO_AM_I
            = System.getProperty( "user.name" );

    private File baseDirectory;
    private File scoreFile;

    private final Set<Score> all_scores = new TreeSet<>();
    private final Set<Score> own_scores = new TreeSet<>();
    
    public class Score {

        private final String who;
        private final long when;
        private final long score;

        private Score( String who, long when, long score ) {
            this.who = who;
            this.when = when;
            this.score = score;
        }

        public Score( long when, long score ) {
            this( WHO_AM_I, when, score );
        }

        public long getWhen() {
            return when;
        }

        public long getScore() {
            return score;
        }

        public String getWho() {
            return who;
        }
    }

    public HallOfFame( MineSweeper ms, String base_dir ) {
        super( format( INTL.getString(
                "HALL_OF_FAME_DIALOG_NAME" ),
                ms.getRows(),
                ms.getCols(),
                ms.getMinesToMark() ) );
        setDefaultCloseOperation( HIDE_ON_CLOSE);
        
        baseDirectory = new File( base_dir );
        scoreFile = new File( baseDirectory,
                format( "Score-{0}x{1}-M={2}-O:{3}.properties",
                        ms.getRows(),
                        ms.getCols(),
                        ms.getMinesToMark(),
                        WHO_AM_I ) );

        final Pattern pattern
                = Pattern.compile(
                        format( "Score-{0}x{1}-M={2}-O=(.*)\\.properties",
                                ms.getRows(),
                                ms.getCols(),
                                ms.getMinesToMark(),
                                WHO_AM_I ) );
        FilenameFilter fnm = (d, n) -> pattern.matcher( n ).matches();
        
        

        LOG.info( format(
                "baseDirectory = {0}",
                baseDirectory ) );
        LOG.info( format(
                "scoreFile = {0}",
                scoreFile ) );
        try {
            for ( File f : baseDirectory.listFiles( fnm ) ) {
                LOG.info( ()
                        -> format( INTL.getString(
                                "READING_FROM_FILE" ),
                                f.getName() ) );
                Properties props = new Properties();
                try {
                    props.load( new FileReader( f ) );

                } catch ( IOException ex ) {
                    LOG.warning( () -> format(
                            INTL.getString( "LOAD_ERROR" ),
                            f,
                            ex ) );
                    continue;
                }
                props.forEach( (key, val) -> {
                    long score = Long.parseLong( (String) key );
                    String[] fields = ((String) val)
                            .split( "\\s*(,\\s*|\\s+)" );
                    if ( fields.length != 3 ) {
                        LOG.warning( () -> format(
                                "ERROR_PARSING_{0}_{1}_{2}",
                                f, key, val ) );
                        return;
                    }
                    String name = fields[ 0 ];
                    long when = Long.parseLong( fields[ 1 ] );
                    int read_hash = Integer.parseInt( fields[ 2 ] );
                    Score sc = new Score( name, when, score );
                    int expected = sc.hashCode();
                    if ( read_hash != expected ) {
                        LOG.warning( () -> format(
                                "HASH_FAILURE_{0}_{1}_{2}_{3}",
                                f, key, val,
                                read_hash, expected ) );
                        return;
                    }
                    all_scores.add( sc );
                    if ( name.equals( WHO_AM_I ) ) {
                        own_scores.add( sc );
                    }
                } );
            }
        } catch ( NullPointerException ex ) {
            LOG.warning( format(
                    INTL.getString( "CANNOT_READ_BASE_DIR" ),
                    baseDirectory ) );
        }
    }
}
