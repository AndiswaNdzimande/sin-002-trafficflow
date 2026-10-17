package co.wethinkcode.trafficflow;

import io.javalin.Javalin;

import java.util.Map;                              // NEW

/**
 * Congestion service (port 7022): tracks the city-wide congestion level (0-8).
 *
 * <ul>
 *   <li>{@code GET /health}      - "OK" if alive</li>
 *   <li>{@code GET /congestion}  - current level, e.g. {"level": 3}</li>
 *   <li>{@code PUT /congestion}  - set the level with {"level": 0-8}; 400 if invalid</li>
 * </ul>
 */
public class CongestionServiceApp {

    public static void main(String[] args) {
        CongestionState state = new CongestionState();      // NEW

        Javalin app = Javalin.create().start(7022);

        app.get("/health", ctx -> ctx.result("OK"));

        // NEW endpoints
        app.get("/congestion", ctx -> ctx.json(new CongestionLevel(state.get())));

        app.put("/congestion", ctx -> {
            CongestionLevel body;
            try {
                body = ctx.bodyAsClass(CongestionLevel.class);
            } catch (Exception e) {                          // not JSON, or wrong shape
                body = null;
            }
            if (body == null || body.level() == null) {
                ctx.status(400).json(Map.of("error", "Body must be JSON like {\"level\": 5}"));
                return;
            }
            try {
                state.set(body.level());
                ctx.json(body);
            } catch (IllegalArgumentException e) {           // outside 0-8
                ctx.status(400).json(Map.of("error", e.getMessage()));
            }
        });
    }
}

// MQ TODO: publishes to ActiveMQ topic MqConfig.TOPIC at MqConfig.BROKER_URL (see co.wethinkcode.trafficflow.mq.MqConfig)