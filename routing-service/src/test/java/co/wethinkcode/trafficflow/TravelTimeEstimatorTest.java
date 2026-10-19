package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TravelTimeEstimatorTest {

    private static Intersection of(String signalType) {
        return new Intersection("INT-X", "Downtown", signalType, true);
    }

    private static double estimate(int level, String fromType, String toType) {
        return TravelTimeEstimator.estimateMinutes(level, of(fromType), of(toType));
    }

    @Test
    void noCongestionAddsOnlySignalDelays() {
        assertEquals(14.0, estimate(0, "4-way", "4-way"), 0.001);
        assertEquals(11.5, estimate(0, "roundabout", "pedestrian"), 0.001);
    }

    @Test
    void congestionScalesTheWholeTrip() {
        assertEquals(21.0, estimate(4, "4-way", "4-way"), 0.001);
        assertEquals(28.0, estimate(8, "4-way", "4-way"), 0.001);
    }

    @Test
    void unknownSignalTypeUsesTheDefaultDelay() {
        assertEquals(13.0, estimate(0, "4-way", null), 0.001);
    }

    @Test
    void higherCongestionNeverMakesATripFaster() {
        for (int level = 0; level < 8; level++) {
            assertTrue(estimate(level + 1, "4-way", "stop-sign") >= estimate(level, "4-way", "stop-sign"));
        }
    }
}