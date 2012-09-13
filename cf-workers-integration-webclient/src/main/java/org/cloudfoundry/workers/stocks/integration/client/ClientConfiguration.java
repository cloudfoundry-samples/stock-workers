/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.workers.stocks.integration.client;

import java.util.Collection;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.messaging.RabbitServiceCreator;
import org.cloudfoundry.workers.common.config.CloudRabbitConnectionFactoryConfiguration;
import org.cloudfoundry.workers.common.config.LocalRabbitConnectionFactoryConfiguration;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.util.Assert;

/**
 * Configures the service's gateway client which in turn communicates with the
 * remote service through a Spring Integration gateway implementation.
 * 
 * @author Josh Long (josh.long@springsource.com)
 */
@ImportResource("classpath:/client.xml")
@Import({LocalRabbitConnectionFactoryConfiguration.class, CloudRabbitConnectionFactoryConfiguration.class})
@Configuration
public class ClientConfiguration {

	private String tickers = "tickers";

	@Autowired
	private Environment environment;

	@Bean
	public RabbitTemplate amqpTemplate() {
		RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
		rabbitTemplate.setMessageConverter(mc());
		return rabbitTemplate;
	}

	@Bean
	public RabbitTransactionManager amqpTransactionManager() {
		return new RabbitTransactionManager(this.connectionFactory());
	}

	@Bean
	public MessageConverter mc() {
		return new JsonMessageConverter();
	}

	@Bean
	public ConnectionFactory connectionFactory() {

		CloudEnvironment cloudEnvironment = this.cloudEnvironment();
		Collection<RabbitServiceInfo> rabbitServiceInfoList = cloudEnvironment
				.getServiceInfos(RabbitServiceInfo.class);
		Assert.isTrue(rabbitServiceInfoList.size() > 0,
				"the rabbitService infos collection should be > 0");
		RabbitServiceInfo rabbitServiceInfo = rabbitServiceInfoList.iterator()
				.next();
		RabbitServiceCreator rabbitServiceCreator = new RabbitServiceCreator();
		return rabbitServiceCreator.createService(rabbitServiceInfo);
	}

	@Bean
	public CloudEnvironment cloudEnvironment() {
		return new CloudEnvironment();
	}

	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(this.connectionFactory());
	}

	@Bean
	public Queue customerQueue() {
		Queue q = new Queue(this.tickers);
		amqpAdmin().declareQueue(q);
		return q;
	}

	@Bean
	public DirectExchange customerExchange() {
		DirectExchange directExchange = new DirectExchange(tickers);
		this.amqpAdmin().declareExchange(directExchange);
		return directExchange;
	}

	@Bean
	public Binding marketDataBinding() {
		return BindingBuilder.bind(customerQueue()).to(customerExchange())
				.with(this.tickers);
	}

	static class MyLogger {
		@ServiceActivator
		public void log(org.springframework.integration.Message<?> msg)
				throws Throwable {
			Object payload = msg.getPayload();
			System.out.println(ToStringBuilder.reflectionToString(payload));
		}
	}

	@Bean
	public MyLogger logger() {
		return new MyLogger();
	}

}
