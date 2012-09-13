package org.cloudfoundry.workers.stocks.batch;

import javax.sql.DataSource;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;


/**
 * @author Oleg Zhurakousky
 */
@Configuration
@Profile("local")
public class LocalDataSourceConfiguration implements DataSourceConfiguration {

	@Override
	public DataSource dataSource() {
		SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
		dataSource.setPassword("");
		dataSource.setUrl("jdbc:h2:tcp://localhost/~/stocks");
		dataSource.setUsername("sa");
		dataSource.setDriverClass(org.h2.Driver.class);
		return dataSource;
	}

}
