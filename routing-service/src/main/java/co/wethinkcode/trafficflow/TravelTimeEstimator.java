package co.wethinkcode.trafficflow;

import java.util.Locale;
import java.util.Map;

/** Pure calculation of an estimated trip time. No network, so it is easy to test. */
public final class TravelTimeEstimator {

    static final double BASE_MINUTES = 10.0;
    static final double DEFAULT_SIGNAL_DELAY = 1.0;

    private static final Map<String, Double> SIGNAL_DELAY = Map.of(
            "4-way", 2.0,
            "stop-sign", 1.5,
            "pedestrian", 1.0,
            "roundabout", 0.5);

    private TravelTimeEstimator() {}

    /** Congestion level 0 = normal; level 8 doubles the trip time. */
    public static double estimateMinutes(int congestionLevel, Intersection from, Intersection to) {
        double signalDelay = delayFor(from) + delayFor(to);
        double congestionMultiplier = 1.0 + congestionLevel / 8.0;
        double minutes = (BASE_MINUTES + signalDelay) * congestionMultiplier;
        return Math.round(minutes * 10) / 10.0;   // one decimal place
    }

    private static double delayFor(Intersection intersection) {
        if (intersection.signalType() == null) return DEFAULT_SIGNAL_DELAY;
        return SIGNAL_DELAY.getOrDefault(
                intersection.signalType().toLowerCase(Locale.ROOT), DEFAULT_SIGNAL_DELAY);
    }
}
