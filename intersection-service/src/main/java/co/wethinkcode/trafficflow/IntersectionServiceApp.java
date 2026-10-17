package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.slf4j.Logger;                          // NEW
import org.slf4j.LoggerFactory;                   // NEW

import java.util.LinkedHashMap;                   // NEW
import java.util.Locale;                          // NEW
import java.util.Map;                             // NEW

/**
 * Intersection service (port 7021): the source of truth for which intersections exist.
 *
 * <p>Loads the cleaned list from ingestion-service at startup and serves it.
 * <ul>
 *   <li>{@code GET /health}                - "OK" if alive</li>
 *   <li>{@code GET /intersections}         - all intersections</li>
 *   <li>{@code GET /intersections/{id}}    - one intersection, or 404 if unknown</li>
 * </ul>
 */
public class IntersectionServiceApp {

    private static final Logger log = LoggerFactory.getLogger(IntersectionServiceApp.class); // NEW

    public static void main(String[] args) {
        // NEW: load the canonical list from ingestion before accepting traffic
        String ingestionUrl = System.getenv().getOrDefault("INGESTION_URL", "http://localhost:7020");
        Map<String, Intersection> byId = new LinkedHashMap<>();
        for (Intersection i : new IngestionClient(ingestionUrl).fetchWithRetry(5, 2000)) {
            byId.put(i.id(), i);
        }
        log.info("Loaded {} intersections from {}", byId.size(), ingestionUrl);

        Javalin app = Javalin.create().start(7021);

        app.get("/health", ctx -> ctx.result("OK"));

        // NEW endpoints
        app.get("/intersections", ctx -> ctx.json(byId.values()));
        app.get("/intersections/{id}", ctx -> {
            String id = ctx.pathParam("id").trim().toUpperCase(Locale.ROOT); // int-1001 works too
            Intersection found = byId.get(id);
            if (found == null) {
                ctx.status(404).json(Map.of("error", "Unknown intersection: " + id));
            } else {
                ctx.json(found);
            }
        });
    }
}

// MQ TODO: publishes a periodic heartbeat to ActiveMQ queue MqConfig.HEARTBEAT_QUEUE at
// MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig), consumed by intersection-watchdog.