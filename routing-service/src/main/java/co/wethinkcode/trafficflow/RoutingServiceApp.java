package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Routing service (port 7023): estimates travel time between two intersections.
 *
 * <ul>
 *   <li>{@code GET /health}                          - "OK" if alive</li>
 *   <li>{@code GET /route?from=INT-1001&to=INT-1004} - estimate; 400 if a parameter is missing,
 *       404 if an intersection is unknown, 503 if a dependency is unavailable</li>
 * </ul>
 */
public class RoutingServiceApp {

    private static final Logger log = LoggerFactory.getLogger(RoutingServiceApp.class);   // NEW

    public static void main(String[] args) {
        // NEW: where the other services live (overridable without changing code)
        IntersectionClient intersections = new IntersectionClient(
                System.getenv().getOrDefault("INTERSECTION_URL", "http://localhost:7021"));
        CongestionClient congestion = new CongestionClient(
                System.getenv().getOrDefault("CONGESTION_URL", "http://localhost:7022"));

        Javalin app = Javalin.create().start(7023);

        app.get("/health", ctx -> ctx.result("OK"));

        // NEW
        app.get("/route", ctx -> {
            String from = normalise(ctx.queryParam("from"));
            String to = normalise(ctx.queryParam("to"));
            if (from == null || to == null) {
                ctx.status(400).json(Map.of("error", "Both 'from' and 'to' query parameters are required"));
                return;
            }

            Optional<Intersection> start = intersections.find(from);
            Optional<Intersection> end = intersections.find(to);
            if (start.isEmpty() || end.isEmpty()) {
                String unknown = start.isEmpty() ? from : to;
                ctx.status(404).json(Map.of("error", "Unknown intersection: " + unknown));
                return;
            }

            int level = congestion.currentLevel();
            double minutes = TravelTimeEstimator.estimateMinutes(level, start.get(), end.get());
            ctx.json(new RouteEstimate(start.get().id(), end.get().id(), level, minutes));
        });

        // NEW: any dependency failure anywhere above becomes a clear 503
        app.exception(UpstreamUnavailableException.class, (e, ctx) -> {
            log.warn("Upstream problem: {}", e.getMessage());
            ctx.status(503).json(Map.of("error", "Cannot calculate route right now: " + e.getMessage()));
        });
    }

    private static String normalise(String id) {
        return (id == null || id.isBlank()) ? null : id.trim().toUpperCase(Locale.ROOT);
    }
}

// MQ TODO: subscribes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)
