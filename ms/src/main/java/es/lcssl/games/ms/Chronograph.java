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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import static java.text.MessageFormat.format;
import java.util.logging.Logger;

/**
 * A {@code Chronograph} is the closest to a manual chronograph to measure time
 * that we have been able to implement in software. It works like a handheld
 * chronograph, with a {@link #start() } button, a {@link #stop() } button
 *
 * @author lcu
 */
public class Chronograph implements Runnable {

    private static final Logger LOG
            = Logger.getLogger(Chronograph.class.getSimpleName());

    /**
     * {@link PropertyChangeListener}s registered on this property are notified
     * of realtime changes at intervals specified by the
     */
    public static final String PROPERTY_TIMESTAMP = "timestamp";

    /**
     * The pace at which updates are signalled to screen display widgets. The
     * value specified is bout 24 notifies per second, approx. the display
     * refresh time. The {@link Chronograph} starts a {@link Thread} when
     * started, that loops on this time, to notify all listeners that they have
     * to update the time. This thread runs while the
     * {@link Chronograph#isStarted() } returns {@code true}.
     */
    public static final long WHEN_TO_RUN = 42;

    /**
     * This value stores the starting time of the Chronograph. The returned time
     * of a Chronograph object is so, the difference between actual time (as
     * returned by {@link System#currentTimeMillis() }) and this field (in
     * millisec) and can be computed without drifting the computed value or
     * affecting the precision of the measurement.
     */
    long startTime = 0;

    /**
     * This value stores the starting time of the Chronograph. It is used to
     * calculate the elapsed time from the {@link #startTime}, above.
     */
    boolean started = false;

    /**
     * Each time a new value is obtained from the Chronograph, the value is
     * stored in this field, in order to have a valid (frozen) value, when the
     * Chronograph is stopped. So, the value of {@link #last_value} is used when
     * the Chronograph instance has been stopped, and the value is instead
     * calculated, when the Chronograph is running.
     */
    long last_value = 0, prev_value = 0;

    /**
     * This allows to signal any screen widget on a regular basis, to allow
     * screen updating in real time. Every {@link PropertyChangeListener}
     * registered for {@link #PROPERTY_TIMESTAMP} events will be notified
     * regularly of changes in time, so the screen reflects accurately the
     * elapsed time.
     */
    PropertyChangeSupport propertyChange
            = new PropertyChangeSupport(this);

    /**
     * Updating thread. This thread is created/started at {@link #start()} and
     * runs until the {@link #started} field is set to {@code false}. After
     * that, the {@link #run()} method detects that {@link #started} is set to
     */
    private Thread updatingThread;

    /**
     * This method is responsible of taking a sample of the time to be displayed
     * on the output widgets that are showing the time in real time. For that,
     * first a sample is taken (which updates the {@link #last_value} field, and
     * a {@link PropertyChangeEvent} is sent to all listeners waiting for it to
     * be displayed.
     */
    protected void update() {
        prev_value = last_value;
        last_value = getTimeMillis();
        if (prev_value != last_value) {
                propertyChange.firePropertyChange(
                        PROPERTY_TIMESTAMP,
                        toString(prev_value),
                        toString(last_value));
                LOG.finest("update()");
        }
    }

    /**
     * This method sets the {@link #startTime} to the current time, so if we
     * read the {@link Chronograph} we will get a reading of 0. A final call to
     * {@link #update()} is made to refresh anything on screen.
     */
    public synchronized void reset() {
        started = false; // so, chrono is stopped, any started thread will stop
        startTime = System.currentTimeMillis(); // so time starts now
        last_value = 0; // so duration shows 0
        LOG.info("reset() called\n");
        update();
    }

    /**
     * This method starts the {@code Chronograph} by setting starting to
     * {@code true} and starting a new Thread to notify the listeners of the
     * time updates. A final call to {@link #update() } is made to refresh
     * everything on the screen.
     */
    public synchronized void start() {
        if (started) {
            return; // already started.
        }
        startTime = System.currentTimeMillis();
        started = true;
        updatingThread = new Thread(this);
        updatingThread.start();
        LOG.info("start() called\n");
        update();
    }

    /**
     * This method simply stops the update of data on the screen and sets
     * {@link #started} to {@code false}, so no more updates are done.
     */
    public synchronized void stop() {
        started = false;
        /* now we are stopped. */
        LOG.info("stop() called\n");
        update();
    }

    public synchronized boolean isStarted() {
        return started;
    }

    @Override
    public void run() {
        while (isStarted()) {
            try {
                Thread.sleep(WHEN_TO_RUN);
                update();
            } catch (InterruptedException e) {
                // Just ignore the exception, as the next loop test will fail.
            }
        }
    }

    public void addValueChangeListener(
            String name,
            PropertyChangeListener listener) {
        propertyChange.addPropertyChangeListener(name, listener);
        LOG.fine(format("Add listener {0} to {1}",
                        listener, name));
    }

    public void removeValueChangeListener(
            String name,
            PropertyChangeListener listener) {
        propertyChange.removePropertyChangeListener(name, listener);
        LOG.fine(format("Remove listener {0} to {1}",
                        listener, name));
    }

    public long getTimeMillis() {
        if (isStarted()) {
            return System.currentTimeMillis() - startTime;
        } else {
            return last_value;
        }
    }

    private static enum Unit {
        YEAR(365 * 86_400_000L, "{0}y"),
        WEEK(7 * 86_400_000L, "{0}w"),
        DAY(86_400_000L, "{0}d"),
        HOUR(3600_000L, "{0}h"),
        MIN(60_000L, "{0}m"),
        SEC(1000L, "{0}s"),
        MSEC(1L, "{0,number,000}ms");

        private long value;
        private String format;

        Unit(long val, String nam) {
            value = val;
            format = nam;
        }
    }

    public static String toString(long value) {
        if (value == 0) {
            return format("     " + Unit.MSEC.format, 0L);
        }
        StringBuilder sb = new StringBuilder();
        String sep = "";
        for (Unit u : Unit.values()) {
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
        return toString(last_value);
    }
}
