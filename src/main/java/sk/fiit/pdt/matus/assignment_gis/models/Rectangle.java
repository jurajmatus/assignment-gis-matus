package sk.fiit.pdt.matus.assignment_gis.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Rectangle {

	private final LatLng point1;
	
	private final LatLng point2;

	public Rectangle(@JsonProperty("point1") LatLng topLeft, @JsonProperty("point2") LatLng bottomRight) {
		this.point1 = topLeft;
		this.point2 = bottomRight;
	}

	@JsonProperty
	public LatLng getPoint1() {
		return point1;
	}

	@JsonProperty
	public LatLng getPoint2() {
		return point2;
	}
	
}
