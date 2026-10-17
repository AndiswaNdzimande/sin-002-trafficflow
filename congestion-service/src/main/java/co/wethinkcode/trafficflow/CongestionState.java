package co.wethinkcode.trafficflow;

import java.util.concurrent.atomic.AtomicInteger;

/** Holds the city-wide congestion level (0-8). Safe to use from many request threads. */
public final class CongestionState {

    public static final int MIN = 0;
    public static final int MAX = 8;

    private final AtomicInteger level = new AtomicInteger(MIN);

    public int get() {
        return level.get();
    }

    /** Sets the level; returns true if it actually changed. Rejects values outside 0-8. */
    public boolean set(int newLevel) {
        if (newLevel < MIN || newLevel > MAX) {
            throw new IllegalArgumentException(
                    "Congestion level must be between " + MIN + " and " + MAX + ", got " + newLevel);
        }
        return level.getAndSet(newLevel) != newLevel;
    }
}
