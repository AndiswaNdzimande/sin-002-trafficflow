package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** One intersection, as received from ingestion-service. Unknown JSON fields are ignored. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Intersection(String id, String district, String signalType, Boolean active) {}