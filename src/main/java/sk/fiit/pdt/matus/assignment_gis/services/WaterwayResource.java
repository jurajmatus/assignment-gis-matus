package sk.fiit.pdt.matus.assignment_gis.services;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

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
import sk.fiit.pdt.matus.assignment_gis.models.Rectangle;

@Path("waterways")
public class WaterwayResource {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(WaterwayResource.class);
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	private final static String SQL_FIND_IN_RECTANGLE = "SELECT"
			+ " name, ST_AsGeoJson(wkb_geometry) geojson"
			+ " FROM geodata"
			+ " WHERE ST_Intersects(wkb_geometry, ST_MakeEnvelope(?, ?, ?, ?, 4326))";
	
	@Inject
	private DbConnection dbConn;
	
	@POST
	@Path("in-rectangle")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<GeoJsonObject> findInRectangle(Rectangle rectangle) throws SQLException {
		
		BigDecimal[] lats = {rectangle.getPoint1().getLat(), rectangle.getPoint2().getLat()};
		BigDecimal[] lngs = {rectangle.getPoint1().getLng(), rectangle.getPoint2().getLng()};
		Arrays.sort(lats);
		Arrays.sort(lngs);
		
		return dbConn.getStream(SQL_FIND_IN_RECTANGLE, st -> {
			st.setBigDecimal(1, lngs[0]);
			st.setBigDecimal(2, lats[0]);
			st.setBigDecimal(3, lngs[1]);
			st.setBigDecimal(4, lats[1]);
		}).map(row -> {
			String geojson = row.get("geojson").toString();
			try {
				return MAPPER.readValue(geojson, GeoJsonObject.class);
			} catch (IOException e) {
				LOGGER.warn("Corrupt geojson column received from db: {}", geojson, e);
				return null;
			}
		}).filter(g -> g != null)
			.collect(toList());
		
	}

}
