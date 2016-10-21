package sk.fiit.pdt.matus.assignment_gis.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;

public class AppConfiguration extends Configuration {

	private DatabaseConfiguration db;

	@JsonProperty
	public DatabaseConfiguration getDb() {
		return db;
	}

	@JsonProperty
	public void setDb(DatabaseConfiguration db) {
		this.db = db;
	}
	
}
