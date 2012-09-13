package org.cloudfoundry.workers.stocks.batch;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.relational.RdbmsServiceCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

import javax.sql.DataSource;
import java.util.Collection;

/**
 * @author Oleg Zhurakousky
 */
@Configuration
@Profile("cloud")
public class CloudDataSourceConfiguration implements DataSourceConfiguration {

    @Bean
    public CloudEnvironment cloudEnvironment() {
        return new CloudEnvironment();
    }

    @Override
    public DataSource dataSource() {
        Collection<RdbmsServiceInfo> servicesInfosForTheDbms = this.cloudEnvironment().getServiceInfos(RdbmsServiceInfo.class);
        Assert.isTrue(servicesInfosForTheDbms.size() > 0, "please ensure that you have created a PostgreSQL RDBMS and " + "bound it appropriately to your Cloud Foundry application instance.");
        RdbmsServiceInfo rdbmsServiceInfo = servicesInfosForTheDbms.iterator().next();
        RdbmsServiceCreator rdbmsServiceCreator = new RdbmsServiceCreator();
        return rdbmsServiceCreator.createService(rdbmsServiceInfo);
    }
}
