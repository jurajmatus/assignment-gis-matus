package sk.fiit.pdt.matus.assignment_gis.models;

import org.geojson.GeoJsonObject;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoJsonFeature {

	static class Properties {
		
		private final String name;
		
		private final String type;

		public Properties(@JsonProperty("name") String name, @JsonProperty("type") String type) {
			this.name = name;
			this.type = type;
		}

		@JsonProperty
		public String getName() {
			return name;
		}

		@JsonProperty
		public String getType() {
			return type;
		}
		
	}
	
	private final Properties properties;
	
	private final GeoJsonObject geometry;

	public GeoJsonFeature(@JsonProperty("name") String name, @JsonProperty("type") String type,
			@JsonProperty("geometry") GeoJsonObject geometry) {
		
		this.properties = new Properties(name, type);
		this.geometry = geometry;
	}
	
	@JsonProperty
	public String getType() {
		return "Feature";
	}

	@JsonProperty
	public Properties getProperties() {
		return properties;
	}

	@JsonProperty
	public GeoJsonObject getGeometry() {
		return geometry;
	}
	
}
