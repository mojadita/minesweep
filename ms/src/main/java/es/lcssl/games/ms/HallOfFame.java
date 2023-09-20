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
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;

/**
 *
 * @author lcu
 */
public class HallOfFame {

    private static final Logger LOG
            = Logger.getLogger( HallOfFame.class.getName() );

    private static final ResourceBundle INTL
            = ResourceBundle.getBundle( HallOfFame.class.getName() );

    private static final String BASE_DIR_NAME
            = INTL.getString( "HALL_OF_FAME_BASE_DIR" );

    private static final File BASE_DIR
            = new File( INTL.getString( BASE_DIR_NAME ) );

    private static final File USER_SCORES
            = new File( BASE_DIR,
                    System.getProperty(
                            "user.name", "user" ) );

    public HallOfFame() {
        LOG.info( format( "BASE_DIR: {0}", BASE_DIR ) );
        LOG.info( format( "USER_SCORES: {0}", USER_SCORES ) );
        if ( !BASE_DIR.isDirectory() ) {
            String msg = format("{0} must be a directory", BASE_DIR_NAME);
            LOG.warning( msg );
            throw new NonDirectoryError(msg );
        }
        
    }

}
