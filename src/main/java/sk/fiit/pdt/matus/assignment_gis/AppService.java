package sk.fiit.pdt.matus.assignment_gis;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.codahale.metrics.health.HealthCheck;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import sk.fiit.pdt.matus.assignment_gis.config.AppConfiguration;
import sk.fiit.pdt.matus.assignment_gis.db.DbConnection;

public class AppService extends Application<AppConfiguration> {

	public static void main(String[] args) throws Exception {
		new AppService().run(args);
	}
	
	@Override
	public void initialize(Bootstrap<AppConfiguration> bootstrap) {
		bootstrap.addBundle(new AssetsBundle("/assets", "/", "index.html"));
	}

	@Override
	public void run(AppConfiguration c, Environment e) throws Exception {
		
		e.healthChecks().register("fake-health-check", new HealthCheck() {
			@Override
			protected Result check() throws Exception {
				return Result.healthy();
			}
		});
		
		e.lifecycle().manage(new DbConnection(c.getDb(), dbConn -> {
			e.jersey().register(new AbstractBinder() {
				@Override
				protected void configure() {
					bind(dbConn).to(DbConnection.class);
				}
			});
		}));
		
	}

}
