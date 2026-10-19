package co.wethinkcode.trafficflow;

/** What GET /route returns. */
public record RouteEstimate(String from, String to, int congestionLevel, double estimatedMinutes) {}
