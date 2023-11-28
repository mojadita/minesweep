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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.swing.AbstractListModel;

import static java.text.MessageFormat.format;

/**
 *
 * @author lcu
 */
public class HallOfFameModel extends AbstractListModel<HallOfFameModel.Score> {

    private static final Logger LOG
            = Logger.getLogger( HallOfFameModel.class.getName() );

    private static final ResourceBundle INTL
            = ResourceBundle.getBundle( HallOfFameModel.class.getName() );

    private static final String WHO_AM_I
            = System.getProperty( "user.name" );

    private File baseDirectory;
    private File scoreFile;

    ArrayList<Score> scores = new ArrayList<>();

    public class Score implements Comparable<Score>, Serializable {

        private int position;
        private final String who;
        private final long when;
        private final long score;

        private Score( String who, long when, long score ) {
            this.who = who;
            this.when = when;
            this.score = score;
        }

        private Score( long when, long score ) {
            this( WHO_AM_I, when, score );
        }

        public int getPosition() {
            return position;
        }

        public void setPosition( int position ) {
            this.position = position;
        }

        public String getWhenAsString() {
            return new Date( when ).toString();
        }

        public String getScoreAsString() {
            return Chronograph.toString( score );
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

        @Override
        public int compareTo( Score o ) {
            int res = Long.compare( score, o.score );
            if ( res != 0 ) {
                return res;
            }
            res = Long.compare( when, o.when );
            if ( res != 0 ) {
                return res;
            }
            return who.compareTo( o.getWho() );
        }

        @Override
        public String toString() {
            return format(
                    INTL.getString( "SCORE_TOSTRING_FORMAT" ),
                    getPosition(),
                    getWho(),
                    getScoreAsString(),
                    getWhenAsString() );
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode( this.who );
            hash = 97 * hash + (int) (this.when ^ (this.when >>> 32));
            hash = 97 * hash + (int) (this.score ^ (this.score >>> 32));
            return hash;
        }

        @Override
        public boolean equals( Object obj ) {
            if ( this == obj ) {
                return true;
            }
            if ( obj == null || getClass() != obj.getClass() ) {
                return false;
            }

            final Score other = (Score) obj;
            return this.when != other.when
                    && this.score != other.score
                    && Objects.equals( this.who, other.who );
        }
    }

    public HallOfFameModel( MineSweeper ms, String base_dir ) {

        baseDirectory = new File( base_dir );
        scoreFile = new File( baseDirectory,
                format( "{0}x{1}-M={2}-O:{3}.score",
                        ms.getRows(),
                        ms.getCols(),
                        ms.getMinesToMark(),
                        WHO_AM_I ) );

        final Pattern pattern
                = Pattern.compile( format(
                        "{0}x{1}-M={2}-O:(.*)\\.score",
                        ms.getRows(),
                        ms.getCols(),
                        ms.getMinesToMark() ) );
        FilenameFilter fnm = (d, n) -> pattern
                .matcher( n ).matches();

        LOG.info( format(
                "baseDirectory = {0}",
                baseDirectory ) );
        LOG.info( format(
                "scoreFile = {0}",
                scoreFile ) );
        scores = new ArrayList<>();
        try {
            for ( File f : baseDirectory.listFiles( fnm ) ) {
                LOG.info( () -> format( INTL.getString(
                        "READING_FROM_FILE" ),
                        f.getName() ) );
                try ( Scanner in = new Scanner(
                        new BufferedInputStream(
                                new FileInputStream( f ) ) ) ) {
                    final int n_args = 4;
                    int line_no = 0;
                    while ( in.hasNext() ) {
                        String line = in.nextLine();
                        final int ln = ++line_no;
                        String[] args = line.split( "[ \t]*:[ \t]*" );
                        if ( args.length != n_args ) {
                            LOG.warning( () -> format(
                                    INTL.getString( "BAD_SYNTAX" ),
                                    ln,
                                    line ) );
                            continue;
                        }
                        try {
                            Score score = new Score(
                                    args[ 0 ],
                                    Long.parseLong( args[ 1 ] ),
                                    Long.parseLong( args[ 2 ] ) );
                            int hash = score.hashCode(), hash_read = Integer.parseInt( args[ 3 ] );
                            if ( hash != hash_read ) {
                                LOG.warning( () -> format(
                                        INTL.getString( "BAD_HASH" ),
                                        ln,
                                        hash_read,
                                        hash ) );
                                continue;
                            }
                            scores.add( score );
                        } catch ( NumberFormatException ex ) {
                            LOG.warning( () -> format(
                                    INTL.getString( "NUMBER_FORMAT" ),
                                    ln,
                                    line,
                                    ex ) );
                            continue;
                        }
                    }
                } catch ( FileNotFoundException ex ) {
                    LOG.warning( () -> format(
                            INTL.getString(
                                    "FILE_NOT_FOUND" ),
                            f,
                            ex ) );
                } catch ( IOException ex ) {
                    LOG.warning( () -> format(
                            INTL.getString(
                                    "LOAD_ERROR" ),
                            f,
                            ex ) );
                }
                scores.sort( Score::compareTo );
                int pos = 1;
                for ( Score s : scores ) {
                    s.setPosition( pos++ );
                }
            }
        } catch ( NullPointerException ex ) {
            LOG.warning( format(
                    INTL.getString( "CANNOT_READ_BASE_DIR" ),
                    baseDirectory ) );
        }
    }

    public void save() throws FileNotFoundException, IOException {
        try ( OutputStreamWriter out = new OutputStreamWriter(
                new FileOutputStream( scoreFile ) ) ) {
            for ( Score s : scores ) {
                out.write( format(
                        "{0}:{1,number,0}:{2,number,0}:{3,number,0}\n",
                        s.getWho(),
                        s.getWhen(),
                        s.getScore(),
                        s.hashCode() ) );
            }
        }
    }

    @Override
    public int getSize() {
        return scores.size();
    }

    @Override
    public Score getElementAt( int index ) {
        return scores.get( index );
    }

    public Score addScore( long when, long score ) {
        Score new_score = new Score( when, score );
        int lft = 0, rgt = scores.size();
        while ( rgt - lft > 1 ) {
            int mid = (lft + rgt) / 2;
            if ( new_score.compareTo( scores.get( mid ) ) < 0 ) {
                rgt = mid;
            } else {
                lft = mid;
            }
        }
        if ( lft == rgt ) {
            /* scores is empty */
            new_score.setPosition( 1 );
            scores.add( 0, new_score );
            fireIntervalAdded( this, 0, 0 );
        } else {
            /* not empty */
            int cmp = new_score.compareTo( scores.get( lft ) );
            if ( cmp < 0 ) {
                /* less than */
                addTheScore( lft, new_score );
            } else if ( cmp > 0 ) {
                /* larger */
                addTheScore( rgt, new_score );
            }
            /* else nothing */
        }
        return new_score;
    }

    private void addTheScore( int where, Score score ) {
        scores.add( where, score );
        int size = scores.size();
        for ( int i = where, pos = i + 1; i < size; i++, pos++ ) {
            scores.get( i ).setPosition( pos );
        }
        fireContentsChanged( this, where, size - 1 );
    }
}
