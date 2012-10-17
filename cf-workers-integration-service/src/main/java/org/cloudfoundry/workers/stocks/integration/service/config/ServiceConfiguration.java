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

package org.cloudfoundry.workers.stocks.integration.service.config;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.cloudfoundry.runtime.env.ApplicationInstanceInfo;
import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.workers.common.config.CloudRabbitConnectionFactoryConfiguration;
import org.cloudfoundry.workers.common.config.LocalRabbitConnectionFactoryConfiguration;
import org.cloudfoundry.workers.common.config.RabbitConnectionFactoryConfiguration;
import org.cloudfoundry.workers.stocks.StockSymbolLookupClient;
import org.cloudfoundry.workers.stocks.YahooPipesQuotesApiStockSymbolLookupClient;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.springframework.web.client.RestTemplate;


/**
 * Configuration for the service that actually does the stock symbol lookup.
 *
 * @author Josh Long (josh.long@springsource.com)
 */
@ImportResource("/symbol-lookup-gateway-service.xml")
@Import({LocalRabbitConnectionFactoryConfiguration.class, CloudRabbitConnectionFactoryConfiguration.class})
@Configuration
public class ServiceConfiguration {

    @Inject
    private RabbitConnectionFactoryConfiguration rabbitConnectionFactoryConfiguration;

    private String stocks = "tickers";

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StockSymbolLookupClient client() {
    	return new YahooPipesQuotesApiStockSymbolLookupClient(restTemplate());    	 
    }
     

    @Bean
    public RabbitTemplate rabbitTemplate() throws Throwable {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(rabbitConnectionFactoryConfiguration.connectionFactory());
        rabbitTemplate.setMessageConverter(mc());
        return rabbitTemplate;
    }

    @Bean
    public RabbitTransactionManager amqpTransactionManager() throws Throwable {
        return new RabbitTransactionManager(rabbitConnectionFactoryConfiguration.connectionFactory());
    }

    @Bean
    public MessageConverter mc() {
        return new JsonMessageConverter();
    }


    @Bean
    public AmqpAdmin amqpAdmin() throws Throwable {
        return new RabbitAdmin(rabbitConnectionFactoryConfiguration.connectionFactory());
    }

    @Bean
    public Queue customerQueue() throws Throwable {
        Queue q = new Queue(this.stocks);
        amqpAdmin().declareQueue(q);
        return q;
    }

    @Bean
    public DirectExchange customerExchange() throws Throwable {
        DirectExchange directExchange = new DirectExchange(stocks);
        this.amqpAdmin().declareExchange(directExchange);
        return directExchange;
    }

    @Bean
    public Binding marketDataBinding() throws Throwable {
        return BindingBuilder.bind(customerQueue()).to(customerExchange()).with(this.stocks);
    }

}
