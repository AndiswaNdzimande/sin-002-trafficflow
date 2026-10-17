package co.wethinkcode.trafficflow;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class IntersectionLoader {

    private IntersectionLoader() {}

    /** Reads a CSV that is packaged inside the jar (src/main/resources). */
    public static List<Intersection> loadFromResource(String resourceName) {
        try (InputStream in = IntersectionLoader.class.getClassLoader()
                .getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new IllegalStateException("Resource not found on classpath: " + resourceName);
            }
            return load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Parses, cleans and de-duplicates CSV text from any source. */
    public static List<Intersection> load(Reader source) {
        Map<String, Intersection> byId = new LinkedHashMap<>(); // keeps file order
        try (CSVReader reader = new CSVReader(source)) {
            String[] header = reader.readNext();
            if (header == null) return List.of();

            Map<String, Integer> col = indexColumns(header);
            if (!col.containsKey("intersection_id")) {
                throw new IllegalStateException("CSV has no intersection_id column");
            }

            String[] row;
            while ((row = reader.readNext()) != null) {
                Intersection cleaned = cleanRow(row, col);
                if (cleaned != null) {
                    byId.merge(cleaned.id(), cleaned, IntersectionLoader::merge);
                }
            }
        } catch (IOException | CsvValidationException e) {
            throw new IllegalStateException("Could not read CSV", e);
        }
        return List.copyOf(byId.values());
    }

    /** "District " (with a stray space) becomes "district" -> column index. */
    private static Map<String, Integer> indexColumns(String[] header) {
        Map<String, Integer> col = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String name = header[i].replace("\uFEFF", "")   // invisible BOM some exporters add
                    .trim().toLowerCase(Locale.ROOT);
            col.put(name, i);
        }
        return col;
    }

    private static String cell(String[] row, Map<String, Integer> col, String name) {
        Integer i = col.get(name);
        return (i == null || i >= row.length) ? null : row[i];
    }

    private static Intersection cleanRow(String[] row, Map<String, Integer> col) {
        String id = Cleaner.cleanId(cell(row, col, "intersection_id"));
        if (id == null) return null; // no ID means we cannot identify the record, so skip it (this also skips blank lines)
        return new Intersection(
                id,
                Cleaner.cleanDistrict(cell(row, col, "district")),
                Cleaner.cleanSignalType(cell(row, col, "signal_type")),
                Cleaner.cleanActive(cell(row, col, "active_flag")));
    }

    /** Two rows for the same intersection: first non-null value wins, per field. */
    private static Intersection merge(Intersection a, Intersection b) {
        return new Intersection(
                a.id(),
                a.district() != null ? a.district() : b.district(),
                a.signalType() != null ? a.signalType() : b.signalType(),
                a.active() != null ? a.active() : b.active());
    }
}
