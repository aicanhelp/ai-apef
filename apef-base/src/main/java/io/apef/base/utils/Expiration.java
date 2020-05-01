package io.apef.base.utils;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A not exact expiration implementation
 * Because the performance of System.currentTimeMillis is very poor.
 * So, here use a timer to update the current time by a 100 milliseconds delay
 */
public class Expiration {

    protected final static int FROM_TIME = generateTimeFrom();

    private int expirationSecs;
    private int offSetRange;
    private int offset;
    private static volatile int currentTime = (int) (System.currentTimeMillis() / 1000L);

    static {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                currentTime = (int) (System.currentTimeMillis() / 1000L);
            }
        }, 0, 100);
    }

    private static int generateTimeFrom() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2010, 1, 1, 0, 0, 0);
        return (int) (calendar.getTimeInMillis() / 1000L);
    }

    public Expiration(int expirationSecs, int offSetRange) {
        this.expirationSecs = expirationSecs;
        this.offSetRange = offSetRange;
        if (offSetRange > 0 && expirationSecs / offSetRange < 3) {
            this.offSetRange = 0;
        }
    }

    /**
     * The response is not absolutely time, it must be used Expiration.isExpired to check
     *
     * @return
     */
    public int expirationSecs() {
        offset = offset + 1;

        if (offset > this.offSetRange) offset = 0;
        return ((currentTime - FROM_TIME) + expirationSecs + offset);
    }

    public static int expirationSecs(int expirationSecs) {
        return (currentTime - FROM_TIME) + expirationSecs;
    }

    public static boolean isExpired(int seconds) {
        return (currentTime - FROM_TIME) >= seconds;
    }
}
