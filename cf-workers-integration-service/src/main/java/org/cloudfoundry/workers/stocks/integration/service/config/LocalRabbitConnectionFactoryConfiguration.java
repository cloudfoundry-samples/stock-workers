package org.cloudfoundry.workers.stocks.integration.service.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Supports configuring RabbitMQ on the cloud
 *
 * @author Josh Long
 */
@Configuration
@Profile("local")
public class LocalRabbitConnectionFactoryConfiguration implements RabbitConnectionFactoryConfiguration {

    @Override
    @Bean
    public ConnectionFactory connectionFactory() throws Throwable {
        return new CachingConnectionFactory("127.0.0.1");
    }
}
