package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The JSON from congestion-service, e.g. {"level": 3}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CongestionLevel(Integer level) {}
