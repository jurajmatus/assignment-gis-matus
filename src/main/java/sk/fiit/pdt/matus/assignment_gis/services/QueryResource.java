package sk.fiit.pdt.matus.assignment_gis.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.beanutils.DynaBean;
import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static java.util.stream.Collectors.toList;

import sk.fiit.pdt.matus.assignment_gis.db.DbConnection;
import sk.fiit.pdt.matus.assignment_gis.db.DbConnection.SQLValueBinder;
import sk.fiit.pdt.matus.assignment_gis.models.DistanceRestriction;
import sk.fiit.pdt.matus.assignment_gis.models.GeoJsonFeature;
import sk.fiit.pdt.matus.assignment_gis.models.LatLng;
import sk.fiit.pdt.matus.assignment_gis.models.Rectangle;
import sk.fiit.pdt.matus.assignment_gis.models.SearchCriteria;

@Path("query")
public class QueryResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryResource.class);
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	private final static String SQL_BASE_SELECT = "SELECT"
			+ " id, name, type, ST_AsGeoJson(wkb_geometry) geometry"
			+ " FROM geodata"
			+ " WHERE";
	
	private final static String SQL_FIND_IN_RECTANGLE = SQL_BASE_SELECT
			+ " (0 = ? OR ST_Intersects(wkb_geometry, ST_MakeEnvelope(?, ?, ?, ?, 4326)))"
			+ " AND (0 = ? OR type = ANY (?))"
			+ " AND (0 = ? OR ST_Area(ST_TRANSFORM(wkb_geometry, 2163)) >= ?)"
			+ " AND (0 = ? OR ST_Area(ST_TRANSFORM(wkb_geometry, 2163)) <= ?)";
	
	private final static String SQL_FIND_CLOSEST = SQL_BASE_SELECT
			+ " ST_Distance(wkb_geometry, ST_GeomFromText(?, 4326))"
			+ " = (SELECT MIN(ST_Distance(wkb_geometry, ST_GeomFromText(?, 4326))) FROM geodata)";
	
	private final static String SQL_FIND_WITH_HIGHEST_PERMIETER = "WITH perimeters AS ("
			+ SQL_BASE_SELECT.replaceFirst("id", "id, ST_Perimeter(ST_TRANSFORM(wkb_geometry, 2163)) perimeter")
							.replaceFirst("ST_AsGeoJson\\(wkb_geometry\\)", "ST_AsGeoJson(wkb_geometry, 13, 1)")
			+ " ST_Distance(ST_TRANSFORM(wkb_geometry, 2163), ST_TRANSFORM(ST_GeomFromText(?, 4326), 2163)) / 1000 <= ?"
			+ ") "
			+ "SELECT * FROM perimeters WHERE perimeter > 0 ORDER BY perimeter DESC LIMIT 10";
	
	@Inject
	private DbConnection dbConn;
	
	private class StatementBinder implements SQLValueBinder {

		final SearchCriteria criteria;
		
		StatementBinder(SearchCriteria criteria) {
			this.criteria = criteria;
		}

		@Override
		public void bind(PreparedStatement st) throws SQLException {
			
			Rectangle rectangle = criteria.getRectangle();
			if (rectangle == null) {
				
				st.setInt(1, 0);
				for (int i = 2; i <= 5; i++) {
					st.setInt(i, 0);
				}
				
			} else {
				BigDecimal[] lats = {rectangle.getPoint1().getLat(), rectangle.getPoint2().getLat()};
				BigDecimal[] lngs = {rectangle.getPoint1().getLng(), rectangle.getPoint2().getLng()};
				Arrays.sort(lats);
				Arrays.sort(lngs);
				
				st.setInt(1, 1);
				st.setBigDecimal(2, lngs[0]);
				st.setBigDecimal(3, lats[0]);
				st.setBigDecimal(4, lngs[1]);
				st.setBigDecimal(5, lats[1]);
			}
			
			List<String> types = criteria.getTypes();
			if (types == null) {
				st.setInt(6, 0);
				st.setArray(7, dbConn.arrayOfStrings());
			} else {
				st.setInt(6, 1);
				st.setArray(7, dbConn.arrayOfStrings(types));
			}
			
			Double areaFrom = criteria.getAreaFrom();
			if (areaFrom == null) {
				st.setInt(8, 0);
				st.setDouble(9, 0);
			} else {
				st.setInt(8, 1);
				st.setDouble(9, areaFrom);
			}
			
			Double areaTo = criteria.getAreaTo();
			if (areaTo == null) {
				st.setInt(10, 0);
				st.setDouble(11, 0);
			} else {
				st.setInt(10, 1);
				st.setDouble(11, areaTo);
			}
			
		}
		
	}
	
	private static GeoJsonFeature rowToFeature(DynaBean row) {			
		Function<String, String> getString = column -> Optional.ofNullable(row.get(column)).orElse("").toString();
		
		GeoJsonObject geometry;
		try {
			geometry = MAPPER.readValue(getString.apply("geometry"), GeoJsonObject.class);
		} catch (IOException e) {
			LOGGER.warn("Problem mapping geojson geometry", e);
			return null;
		}
		
		return new GeoJsonFeature(getString.apply("name"), getString.apply("type"),
				geometry, ((Double) row.get("id")).longValue());
	}
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<GeoJsonFeature> findInRectangle(SearchCriteria criteria) throws SQLException {
		
		return dbConn.getStream(SQL_FIND_IN_RECTANGLE, new StatementBinder(criteria))
				.map(QueryResource::rowToFeature)
				.filter(g -> g != null)
				.collect(toList());
		
	}
	
	@POST
	@Path("closest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<GeoJsonFeature> findClosest(LatLng center) throws SQLException {
		
		return dbConn.getStream(SQL_FIND_CLOSEST, st -> {
			String point = String.format("POINT(%.8f %.8f)", center.getLng().doubleValue(), center.getLat().doubleValue());
			st.setString(1, point);
			st.setString(2, point);
		})
		.map(QueryResource::rowToFeature)
		.filter(g -> g != null)
		.collect(toList());
		
	}
	
	@POST
	@Path("max-perimeter")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<JsonNode> getWithHighestPerimeter(DistanceRestriction dist) throws SQLException {
		
		return dbConn.getStream(SQL_FIND_WITH_HIGHEST_PERMIETER, st -> {
			String point = String.format("POINT(%.8f %.8f)", dist.getCenter().getLng().doubleValue(),
					dist.getCenter().getLat().doubleValue());
			st.setString(1, point);
			st.setDouble(2, dist.getMaxDistance());
		})
		.map(row -> {
			GeoJsonFeature feature = rowToFeature(row);
			ObjectNode richFeature = MAPPER.valueToTree(feature);
			
			richFeature.put("perimeter", (double) row.get("perimeter"));
			
			return richFeature;
		})
		.filter(g -> g != null)
		.collect(toList());
		
	}

}
