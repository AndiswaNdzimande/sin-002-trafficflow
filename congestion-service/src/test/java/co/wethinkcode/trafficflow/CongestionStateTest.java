package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CongestionStateTest {

    @Test
    void startsAtZero() {
        assertEquals(0, new CongestionState().get());
    }

    @Test
    void acceptsEveryLevelFromZeroToEight() {
        CongestionState state = new CongestionState();
        for (int level = 0; level <= 8; level++) {
            state.set(level);
            assertEquals(level, state.get());
        }
    }

    @Test
    void rejectsOutOfRangeAndKeepsOldValue() {
        CongestionState state = new CongestionState();
        state.set(3);
        assertThrows(IllegalArgumentException.class, () -> state.set(9));
        assertThrows(IllegalArgumentException.class, () -> state.set(-1));
        assertEquals(3, state.get());
    }

    @Test
    void reportsWhetherTheValueChanged() {
        CongestionState state = new CongestionState();
        assertTrue(state.set(5));
        assertFalse(state.set(5));
        assertTrue(state.set(6));
    }
}