package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/** Fetches the cleaned intersection list from ingestion-service over HTTP. */
public final class IngestionClient {

    private static final Logger log = LoggerFactory.getLogger(IngestionClient.class);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))   // never wait forever for a dead service
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final URI url;

    public IngestionClient(String baseUrl) {
        this.url = URI.create(baseUrl + "/intersections");
    }

    /** Tries up to {@code attempts} times, pausing between tries, then gives up loudly. */
    public List<Intersection> fetchWithRetry(int attempts, long delayMillis) {
        Exception last = null;
        for (int i = 1; i <= attempts; i++) {
            try {
                return fetchOnce();
            } catch (Exception e) {
                last = e;
                log.warn("Ingestion not ready (attempt {}/{}): {}", i, attempts, e.toString());
                if (i < attempts) pause(delayMillis);
            }
        }
        throw new IllegalStateException("Could not load intersections from " + url, last);
    }

    private List<Intersection> fetchOnce() throws Exception {
        HttpRequest request = HttpRequest.newBuilder(url)
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Ingestion returned HTTP " + response.statusCode());
        }
        return mapper.readValue(response.body(), new TypeReference<List<Intersection>>() {});
    }

    private static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
