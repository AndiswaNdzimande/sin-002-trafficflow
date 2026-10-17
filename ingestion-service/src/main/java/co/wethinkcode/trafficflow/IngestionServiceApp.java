package co.wethinkcode.trafficflow;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Ingestion service (port 7020): the first stop in the TrafficFlow pipeline.
 *
 * <p>Loads the messy legacy CSV once at startup, cleans it and removes duplicates,
 * then serves the result as JSON for the other services to consume.
 *
 * <ul>
 *   <li>{@code GET /health}        - returns "OK" if the service is alive</li>
 *   <li>{@code GET /intersections} - returns the cleaned intersections as a JSON array</li>
 * </ul>
 */
public class IngestionServiceApp {

    private static final Logger log = LoggerFactory.getLogger(IngestionServiceApp.class);

    public static void main(String[] args) {
        List<Intersection> intersections =
                IntersectionLoader.loadFromResource("intersections-legacy.csv");
        log.info("Loaded {} cleaned intersections", intersections.size());

        Javalin app = Javalin.create().start(7020);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/intersections", ctx -> ctx.json(intersections));
    }
}