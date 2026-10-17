package co.wethinkcode.trafficflow;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IntersectionLoaderTest {

    private static final String HEADER = "intersection_id,District ,signal_type,active_flag\n";

    private static Intersection find(List<Intersection> all, String id) {
        return all.stream().filter(i -> i.id().equals(id)).findFirst().orElseThrow();
    }

    @Test
    void realFileGivesSeventeenUniqueRecords() {
        List<Intersection> all = IntersectionLoader.loadFromResource("intersections-legacy.csv");
        assertEquals(17, all.size());
        assertEquals(1, all.stream().filter(i -> i.id().equals("INT-1005")).count());
    }

    @Test
    void missingValuesStayNullInRealFile() {
        List<Intersection> all = IntersectionLoader.loadFromResource("intersections-legacy.csv");
        assertEquals(new Intersection("INT-1007", "Eastside", null, true), find(all, "INT-1007"));
        assertEquals(new Intersection("INT-1015", null, "4-way", true), find(all, "INT-1015"));
        assertEquals(new Intersection("INT-1013", "Westside", null, null), find(all, "INT-1013"));
    }

    @Test
    void paddedHeaderStillMatches() {
        List<Intersection> out =
                IntersectionLoader.load(new StringReader(HEADER + "INT-1, Downtown ,4-way,Y\n"));
        assertEquals(List.of(new Intersection("INT-1", "Downtown", "4-way", true)), out);
    }

    @Test
    void duplicatesAreMergedFillingGaps() {
        String csv = HEADER + "INT-1,,4-way,\nint-1,Downtown,,Y\n";
        assertEquals(List.of(new Intersection("INT-1", "Downtown", "4-way", true)),
                IntersectionLoader.load(new StringReader(csv)));
    }

    @Test
    void missingIdColumnFailsLoudly() {
        assertThrows(IllegalStateException.class,
                () -> IntersectionLoader.load(new StringReader("foo,bar\n1,2\n")));
    }
}