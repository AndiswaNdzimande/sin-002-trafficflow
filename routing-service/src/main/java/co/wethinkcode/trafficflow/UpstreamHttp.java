package co.wethinkcode.trafficflow;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/** One place for outbound GET calls, so timeouts and failure handling are consistent. */
public final class UpstreamHttp {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(2))
            .build();

    private UpstreamHttp() {}

    /** Returns the response (any status code), or throws if we could not get one at all. */
    public static HttpResponse<String> get(String serviceName, URI uri) {
        try {
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            return HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new UpstreamUnavailableException(serviceName + " call was interrupted", e);
        } catch (IOException e) {   // connection refused, timeout, reset...
            throw new UpstreamUnavailableException(serviceName + " is unreachable", e);
        }
    }
}
