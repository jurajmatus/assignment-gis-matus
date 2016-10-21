package sk.fiit.pdt.matus.assignment_gis.db;

import java.util.function.Consumer;

import io.dropwizard.lifecycle.Managed;
import sk.fiit.pdt.matus.assignment_gis.config.DatabaseConfiguration;

public class DbConnection implements Managed {

	private final DatabaseConfiguration dbConf;
	
	// TODO - change to driver connection class
	private final Consumer<DbConnection> afterStart;
	
	public DbConnection(DatabaseConfiguration dbConf, Consumer<DbConnection> afterStart) {
		this.dbConf = dbConf;
		this.afterStart = afterStart;
	}

	public void start() throws Exception {
		// TODO - add postgresql driver, implement connection
		afterStart.accept(this);
	}

	public void stop() throws Exception {
		// TODO - implement disconnect
	}

}
