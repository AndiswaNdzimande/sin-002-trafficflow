package co.wethinkcode.trafficflow;

import java.util.Locale;
import java.util.Set;

public final class Cleaner {

    private static final Set<String> PLACEHOLDERS =
            Set.of("", "n/a", "tbd", "unknown", "-", "nan", "null");
    private static final Set<String> TRUE_VALUES = Set.of("y", "yes", "1", "true");
    private static final Set<String> FALSE_VALUES = Set.of("n", "no", "0", "false");

    private Cleaner() {}

    /** Trim, squash repeated spaces, and turn placeholders into null. */
    private static String tidy(String raw) {
        if (raw == null) return null;
        String s = raw.trim().replaceAll("\\s+", " ");
        return PLACEHOLDERS.contains(s.toLowerCase(Locale.ROOT)) ? null : s;
    }

    public static String cleanId(String raw) {
        String s = tidy(raw);
        return s == null ? null : s.toUpperCase(Locale.ROOT);
    }

    public static String cleanDistrict(String raw) {
        String s = tidy(raw);
        if (s == null) return null;
        StringBuilder out = new StringBuilder();
        for (String word : s.split(" ")) {
            if (out.length() > 0) out.append(' ');
            out.append(word.substring(0, 1).toUpperCase(Locale.ROOT))
                    .append(word.substring(1).toLowerCase(Locale.ROOT));
        }
        return out.toString();
    }

    public static String cleanSignalType(String raw) {
        String s = tidy(raw);
        return s == null ? null : s.toLowerCase(Locale.ROOT);
    }

    public static Boolean cleanActive(String raw) {
        String s = tidy(raw);
        if (s == null) return null;
        String key = s.toLowerCase(Locale.ROOT);
        if (TRUE_VALUES.contains(key)) return Boolean.TRUE;
        if (FALSE_VALUES.contains(key)) return Boolean.FALSE;
        return null;
    }
}
