package sk.fiit.pdt.matus.assignment_gis.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DatabaseConfiguration {

	private String host;
	
	private String port;
	
	private String username;
	
	private String password;

	@JsonProperty
	public String getHost() {
		return host;
	}

	@JsonProperty
	public void setHost(String host) {
		this.host = host;
	}

	@JsonProperty
	public String getPort() {
		return port;
	}

	@JsonProperty
	public void setPort(String port) {
		this.port = port;
	}

	@JsonProperty
	public String getUsername() {
		return username;
	}

	@JsonProperty
	public void setUsername(String username) {
		this.username = username;
	}

	@JsonProperty
	public String getPassword() {
		return password;
	}

	@JsonProperty
	public void setPassword(String password) {
		this.password = password;
	}
	
}
