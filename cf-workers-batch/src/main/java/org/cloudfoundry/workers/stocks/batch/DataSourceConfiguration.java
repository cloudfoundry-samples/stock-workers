package org.cloudfoundry.workers.stocks.batch;

import javax.sql.DataSource;

/**
 * @author Oleg Zhurakousky
 */
public interface DataSourceConfiguration {
	DataSource dataSource();
}
