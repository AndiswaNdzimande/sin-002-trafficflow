package co.wethinkcode.trafficflow;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** The JSON shape for reading and setting the level, e.g. {"level": 5}. Integer (not int) so a missing field is null. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CongestionLevel(Integer level) {}
