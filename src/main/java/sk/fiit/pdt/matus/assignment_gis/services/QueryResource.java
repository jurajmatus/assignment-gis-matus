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

import org.geojson.GeoJsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.stream.Collectors.toList;

import sk.fiit.pdt.matus.assignment_gis.db.DbConnection;
import sk.fiit.pdt.matus.assignment_gis.db.DbConnection.SQLValueBinder;
import sk.fiit.pdt.matus.assignment_gis.models.GeoJsonFeature;
import sk.fiit.pdt.matus.assignment_gis.models.Rectangle;
import sk.fiit.pdt.matus.assignment_gis.models.SearchCriteria;

@Path("query")
public class QueryResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QueryResource.class);
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	private final static String SQL_FIND_IN_RECTANGLE = "SELECT"
			+ " name, type, ST_AsGeoJson(wkb_geometry) geometry"
			+ " FROM geodata"
			+ " WHERE (0 = ? OR ST_Intersects(wkb_geometry, ST_MakeEnvelope(?, ?, ?, ?, 4326)))"
			+ " AND (0 = ? OR type = ANY (?))"
			+ " AND (0 = ? OR area >= ?)"
			+ " AND (0 = ? OR area <= ?)";
	
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
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<GeoJsonFeature> findInRectangle(SearchCriteria criteria) throws SQLException {
		
		return dbConn.getStream(SQL_FIND_IN_RECTANGLE, new StatementBinder(criteria))
				.map(row -> {
					
					Function<String, String> getString = column -> Optional.ofNullable(row.get(column)).orElse("").toString();
					
					GeoJsonObject geometry;
					try {
						geometry = MAPPER.readValue(getString.apply("geometry"), GeoJsonObject.class);
					} catch (IOException e) {
						LOGGER.warn("Problem mapping geojson geometry", e);
						return null;
					}
					
					return new GeoJsonFeature(getString.apply("name"), getString.apply("type"), geometry);
			
				})
				.filter(g -> g != null)
				.collect(toList());
		
	}

}
