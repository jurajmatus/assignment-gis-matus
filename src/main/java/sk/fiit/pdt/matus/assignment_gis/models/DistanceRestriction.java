package sk.fiit.pdt.matus.assignment_gis.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DistanceRestriction {

	private final LatLng center;
	
	private final double maxDistance;

	public DistanceRestriction(@JsonProperty("center") LatLng center,
			@JsonProperty("maxDistance") double maxDistance) {
		
		this.center = center;
		this.maxDistance = maxDistance;
	}

	@JsonProperty
	public LatLng getCenter() {
		return center;
	}

	@JsonProperty
	public double getMaxDistance() {
		return maxDistance;
	}
	
}
