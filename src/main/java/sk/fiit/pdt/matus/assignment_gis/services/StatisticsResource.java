package sk.fiit.pdt.matus.assignment_gis.services;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.beanutils.DynaBean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static java.util.stream.Collectors.toList;

import sk.fiit.pdt.matus.assignment_gis.db.DbConnection;

@Path("stats")
public class StatisticsResource {
	
	private final static String SQL_GET_TYPES = "SELECT DISTINCT(type) FROM geodata";
	
	private final static String SQL_GET_AREA_STATS = "SELECT MIN(ST_Area(ST_TRANSFORM(wkb_geometry, 2163))) min,"
			+ " MAX(ST_Area(ST_TRANSFORM(wkb_geometry, 2163))) max FROM geodata";
	
	@Inject
	private DbConnection dbConn;
	
	private List<String> types;
	
	private double minArea;
	
	private double maxArea;
	
	@PostConstruct
	private void loadStats() throws SQLException {
		
		types = dbConn.getStream(SQL_GET_TYPES, st -> {})
					.map(row -> row.get("type").toString())
					.collect(toList());
		
		DynaBean stats = dbConn.getOne(SQL_GET_AREA_STATS, st -> {});
		minArea = (double) stats.get("min");
		maxArea = (double) stats.get("max");
		
	}
	
	public Stream<String> getTypes() {
		return types.stream();
	}
	
	@GET
	@Path("all")
	@Produces(MediaType.APPLICATION_JSON)
	public JsonNode getAll() {
		ObjectNode ret = JsonNodeFactory.instance.objectNode();
		
		ArrayNode typesArray = ret.putArray("types");
		types.forEach(typesArray::add);
		
		ret.put("minArea", minArea);
		ret.put("maxArea", maxArea);
		
		return ret;
	}

}
