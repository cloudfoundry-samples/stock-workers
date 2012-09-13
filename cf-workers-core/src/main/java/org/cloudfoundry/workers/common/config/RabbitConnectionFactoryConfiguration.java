package org.cloudfoundry.workers.common.config;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;

/**
 * @author Josh Long
 */
public  interface  RabbitConnectionFactoryConfiguration {
    ConnectionFactory connectionFactory() throws Throwable ;
}
