package org.cloudfoundry.workers.stocks.integration.service.config;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.messaging.RabbitServiceCreator;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.Assert;

import java.util.Collection;

/**
 * @author Josh Long
 */
@Configuration
@Profile("cloud")
public class CloudRabbitConnectionFactoryConfiguration implements RabbitConnectionFactoryConfiguration {
    @Bean
    public CloudEnvironment cloudEnvironment() {
        return new CloudEnvironment();
    }

    @Override    @Bean
    public ConnectionFactory connectionFactory() throws Throwable {

        CloudEnvironment cloudEnvironment = this.cloudEnvironment();
        Collection<RabbitServiceInfo> rabbitServiceInfoList = cloudEnvironment.getServiceInfos(RabbitServiceInfo.class);
        Assert.isTrue(rabbitServiceInfoList.size() > 0, "the rabbitServiceInfos collection should be > 0");
        RabbitServiceInfo rabbitServiceInfo = rabbitServiceInfoList.iterator().next();
        RabbitServiceCreator rabbitServiceCreator = new RabbitServiceCreator();
        return rabbitServiceCreator.createService(rabbitServiceInfo);

    }
}
