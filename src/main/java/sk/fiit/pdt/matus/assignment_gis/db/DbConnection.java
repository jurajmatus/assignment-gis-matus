package sk.fiit.pdt.matus.assignment_gis.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BasicDynaClass;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.lifecycle.Managed;
import sk.fiit.pdt.matus.assignment_gis.config.DatabaseConfiguration;

public class DbConnection implements Managed {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DbConnection.class);

	private final DatabaseConfiguration dbConf;
	
	private final Consumer<DbConnection> afterStart;

	private Connection connection;
	
	public DbConnection(DatabaseConfiguration dbConf, Consumer<DbConnection> afterStart) {
		this.dbConf = dbConf;
		this.afterStart = afterStart;
	}

	public void start() throws Exception {
		String url = String.format("jdbc:postgresql://%s:%s/%s", dbConf.getHost(), dbConf.getPort(), dbConf.getDatabase());
		connection = DriverManager.getConnection(url, dbConf.getUsername(), dbConf.getPassword());
		afterStart.accept(this);
		testConnection();
	}
	
	private void testConnection() throws SQLException {
		DynaBean res = getOne("SELECT COUNT(1) c FROM geodata", st -> {});
		LOGGER.info("Number of geo records: {}", res.get("c"));
	}

	public void stop() throws Exception {
		connection.close();
	}
	
	public RowSetDynaClass execute(String sql, Consumer<PreparedStatement> binder) throws SQLException {
		try (PreparedStatement st = connection.prepareStatement(sql)) {
			binder.accept(st);
			try (ResultSet rs = st.executeQuery()) {
				return new RowSetDynaClass(rs);
			}
		} catch (SQLException e) {
			LOGGER.error("Error executing SQL statement {}", sql, e);
			throw e;
		}
	}
	
	public DynaBean getOne(String sql, Consumer<PreparedStatement> binder) throws SQLException {
		RowSetDynaClass res = execute(sql, binder);
		List<DynaBean> rows = res.getRows();
		if (rows.size() > 0) {
			return rows.get(0);
		} else {
			return new BasicDynaBean(new BasicDynaClass());
		}
	}
	
	public Stream<DynaBean> getStream(String sql, Consumer<PreparedStatement> binder) throws SQLException {
		return execute(sql, binder).getRows().stream();
	}

}
