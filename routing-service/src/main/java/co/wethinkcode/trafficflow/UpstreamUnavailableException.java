package co.wethinkcode.trafficflow;

/** A service we depend on is down, slow, or sent something we cannot use. */
public class UpstreamUnavailableException extends RuntimeException {
    public UpstreamUnavailableException(String message) {
        super(message);
    }

    public UpstreamUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
