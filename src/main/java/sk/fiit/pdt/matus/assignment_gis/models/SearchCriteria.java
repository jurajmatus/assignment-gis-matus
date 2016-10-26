package sk.fiit.pdt.matus.assignment_gis.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchCriteria {

	private final Rectangle rectangle;
	
	private final List<String> types;
	
	private final Double areaFrom;
	
	private final Double areaTo;

	public SearchCriteria(@JsonProperty("rectangle") Rectangle rectangle,
			@JsonProperty("types") List<String> types,
			@JsonProperty("areaFrom") Double areaFrom,
			@JsonProperty("areaTo") Double areaTo) {
		
		this.rectangle = rectangle;
		this.types = types;
		this.areaFrom = areaFrom;
		this.areaTo = areaTo;
	}

	@JsonProperty
	public Rectangle getRectangle() {
		return rectangle;
	}

	@JsonProperty
	public List<String> getTypes() {
		return types;
	}

	@JsonProperty
	public Double getAreaFrom() {
		return areaFrom;
	}

	@JsonProperty
	public Double getAreaTo() {
		return areaTo;
	}
	
}
