package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CleanerTest {

    @Test
    void idIsTrimmedAndUppercased() {
        assertEquals("INT-1003", Cleaner.cleanId("INT-1003 "));
        assertEquals("INT-1005", Cleaner.cleanId("int-1005"));
    }

    @Test
    void districtIsTrimmedAndTitleCased() {
        assertEquals("Downtown", Cleaner.cleanDistrict(" Downtown "));
        assertEquals("Uptown", Cleaner.cleanDistrict("uptown"));
    }

    @Test
    void signalTypeIsLowercased() {
        assertEquals("4-way", Cleaner.cleanSignalType("4-Way"));
        assertEquals("roundabout", Cleaner.cleanSignalType("ROUNDABOUT"));
    }

    @Test
    void placeholdersBecomeNull() {
        assertNull(Cleaner.cleanSignalType(""));
        assertNull(Cleaner.cleanSignalType("unknown"));
        assertNull(Cleaner.cleanDistrict("N/A"));
        assertNull(Cleaner.cleanDistrict(null));
    }

    @Test
    void activeFlagVariants() {
        for (String yes : new String[]{"Y", "yes", "YES", "1", "true", "TRUE"})
            assertEquals(Boolean.TRUE, Cleaner.cleanActive(yes), yes);
        for (String no : new String[]{"n", "N", "no", "0", "FALSE"})
            assertEquals(Boolean.FALSE, Cleaner.cleanActive(no), no);
        assertNull(Cleaner.cleanActive("unknown"));
        assertNull(Cleaner.cleanActive(""));
    }

    @Test
    void rowThirteenKeepsUnknownsAsNull() {
        Intersection cleaned = new Intersection(
                Cleaner.cleanId("INT-1013"),
                Cleaner.cleanDistrict("Westside "),
                Cleaner.cleanSignalType("unknown"),
                Cleaner.cleanActive("unknown"));
        assertEquals(new Intersection("INT-1013", "Westside", null, null), cleaned);
    }
}