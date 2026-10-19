package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** Asks intersection-service about one intersection. */
public final class IntersectionClient {

    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public IntersectionClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /** Empty means "intersection-service says it does not exist". Exceptions mean "could not ask". */
    public Optional<Intersection> find(String id) {
        String encoded = URLEncoder.encode(id, StandardCharsets.UTF_8).replace("+", "%20");
        HttpResponse<String> response = UpstreamHttp.get(
                "intersection-service", URI.create(baseUrl + "/intersections/" + encoded));

        if (response.statusCode() == 404) return Optional.empty();
        if (response.statusCode() != 200) {
            throw new UpstreamUnavailableException(
                    "intersection-service returned HTTP " + response.statusCode());
        }
        try {
            return Optional.of(mapper.readValue(response.body(), Intersection.class));
        } catch (JsonProcessingException e) {
            throw new UpstreamUnavailableException("intersection-service sent unreadable JSON", e);
        }
    }
}
