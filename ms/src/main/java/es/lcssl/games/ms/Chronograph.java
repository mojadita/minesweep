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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import static java.text.MessageFormat.format;

/**
 *
 * @author lcu
 */
public class Chronograph implements Runnable {

    public static final String PROPERTY_TIMESTAMP = "timestamp";
    public static final long WHEN_TO_RUN = 50;

    long startTime = 0;
    boolean started = false;
    String last_value;
    PropertyChangeSupport propertyChange 
            = new PropertyChangeSupport(this);

    /* stopped */
    public synchronized void start() {
        startTime = System.currentTimeMillis();
        Thread thread = new Thread(this);
        started = true;
        thread.start();
    }

    public synchronized void stop() {
        String new_value = toString(getTimeMillis());
        propertyChange.firePropertyChange(
                PROPERTY_TIMESTAMP, 
                last_value, 
                new_value);
        last_value = new_value;
        started = false;
    }

    public synchronized boolean isStarted() {
        return started;
    }
    
    @Override
    public void run() {
        while (isStarted()) {
            try {
                Thread.sleep(WHEN_TO_RUN);
                String new_value = this.toString();
                propertyChange.firePropertyChange(
                        PROPERTY_TIMESTAMP, 
                        last_value, 
                        new_value);
                last_value = new_value;
            } catch (InterruptedException e) {
                // ignore
            }
        }
    };
    
    public void addValueChangeListener(String name, PropertyChangeListener listener) {
        propertyChange.addPropertyChangeListener(name, listener);
    }
    
    public void removeValueChangeListener(String name, PropertyChangeListener listener) {
        propertyChange.removePropertyChangeListener(name, listener);
    }

    public long getTimeMillis() {
        return System.currentTimeMillis() - startTime;
    }

    private static enum Unit {
        YEAR(365 * 86_400_000, "{0}y"),
        WEEK(7 * 86_400_000, "{0}w"),
        DAY(86_400_000, "{0}d"),
        HOUR(3600_000, "{0}h"),
        MIN(60_000, "{0}m"),
        SEC(1000, "{0}s"),
        MSEC(1, "{0,number,000}ms");
        private long value;
        private String format;

        Unit(long val, String nam) {
            value = val;
            format = nam;
        }
    }
    
    public static String toString(long value) {
        if (value == 0) {
            return format(Unit.SEC.format, 0, 0);
        }
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Unit u: Unit.values()) {
            long units = value / u.value;
            value %= u.value;
            if (units != 0) {
                sb.append(sep)
                        .append(format(
                                u.format,
                                units));
                sep = ", ";
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return isStarted() 
                ? toString(System.currentTimeMillis() - startTime) 
                : toString(0);
    }
}
