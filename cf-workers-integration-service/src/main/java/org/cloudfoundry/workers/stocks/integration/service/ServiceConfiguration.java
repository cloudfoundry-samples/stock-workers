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

package org.cloudfoundry.workers.stocks.integration.service;

import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RabbitServiceInfo;
import org.cloudfoundry.runtime.service.messaging.RabbitServiceCreator;
import org.cloudfoundry.workers.stocks.GoogleFinanceStockSymbolLookupClient;
import org.cloudfoundry.workers.stocks.MockStockSymbolLookupClient;
import org.cloudfoundry.workers.stocks.StockSymbolLookupClient;
import org.cloudfoundry.workers.stocks.YahooPipesQuotesApiStockSymbolLookupClient;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.transaction.RabbitTransactionManager;
import org.springframework.amqp.support.converter.JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.util.Collection;


/**
 * Configuration for the service that actually does the stock symbol lookup.
 *
 * @author Josh Long (josh.long@springsource.com)
 */
@ImportResource("/symbol-lookup-gateway-service.xml")
@Configuration
public class ServiceConfiguration {

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
    public RabbitTemplate rabbitTemplate() {
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
    public CloudEnvironment cloudEnvironment() {
        return new CloudEnvironment();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CloudEnvironment cloudEnvironment = this.cloudEnvironment();
        Collection<RabbitServiceInfo> rabbitServiceInfoList = cloudEnvironment.getServiceInfos(RabbitServiceInfo.class);
        Assert.isTrue(rabbitServiceInfoList.size() > 0, "the rabbitService infos collection should be > 0");
        RabbitServiceInfo rabbitServiceInfo = rabbitServiceInfoList.iterator().next();
        RabbitServiceCreator rabbitServiceCreator = new RabbitServiceCreator();
        return rabbitServiceCreator.createService(rabbitServiceInfo);
    }

    @Bean
    public AmqpAdmin amqpAdmin() {
        return new RabbitAdmin(this.connectionFactory());
    }

    @Bean
    public Queue customerQueue() {
        Queue q = new Queue(this.stocks);
        amqpAdmin().declareQueue(q);
        return q;
    }

    @Bean
    public DirectExchange customerExchange() {
        DirectExchange directExchange = new DirectExchange(stocks);
        this.amqpAdmin().declareExchange(directExchange);
        return directExchange;
    }

    @Bean
    public Binding marketDataBinding() {
        return BindingBuilder.bind(customerQueue()).to(customerExchange()).with(this.stocks);
    }

}
