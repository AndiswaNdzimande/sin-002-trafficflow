package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpResponse;

/** Asks congestion-service for the current level. */
public final class CongestionClient {

    private final String baseUrl;
    private final ObjectMapper mapper = new ObjectMapper();

    public CongestionClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int currentLevel() {
        HttpResponse<String> response =
                UpstreamHttp.get("congestion-service", URI.create(baseUrl + "/congestion"));

        if (response.statusCode() != 200) {
            throw new UpstreamUnavailableException(
                    "congestion-service returned HTTP " + response.statusCode());
        }
        try {
            CongestionLevel body = mapper.readValue(response.body(), CongestionLevel.class);
            if (body.level() == null) {
                throw new UpstreamUnavailableException("congestion-service sent no level");
            }
            return body.level();
        } catch (JsonProcessingException e) {
            throw new UpstreamUnavailableException("congestion-service sent unreadable JSON", e);
        }
    }
}
