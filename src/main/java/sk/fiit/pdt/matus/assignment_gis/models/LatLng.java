package sk.fiit.pdt.matus.assignment_gis.models;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LatLng {

	private final BigDecimal lat;
	
	private final BigDecimal lng;

	public LatLng(@JsonProperty("lat") BigDecimal lat, @JsonProperty("lng") BigDecimal lng) {
		this.lat = lat;
		this.lng = lng;
	}

	@JsonProperty
	public BigDecimal getLat() {
		return lat;
	}

	@JsonProperty
	public BigDecimal getLng() {
		return lng;
	}
	
}
