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

package org.cloudfoundry.workers.stocks.batch;


import org.cloudfoundry.runtime.env.CloudEnvironment;
import org.cloudfoundry.runtime.env.RdbmsServiceInfo;
import org.cloudfoundry.runtime.service.relational.RdbmsServiceCreator;
import org.cloudfoundry.workers.stocks.GoogleFinanceStockSymbolLookupClient;
import org.cloudfoundry.workers.stocks.StockSymbolLookup;
import org.cloudfoundry.workers.stocks.StockSymbolLookupClient;
import org.cloudfoundry.workers.stocks.YahooPipesQuotesApiStockSymbolLookupClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.Date;

/**
 * Configures a Spring Batch job that looks at the ticker symbols
 * in the 'STOCKS' table, and then retreives
 * the information for all of the records for that day.
 *
 * @author Josh Long (josh.long@springsource.com)
 */
@Configuration
@ImportResource("/batch.xml")
@EnableScheduling
public class BatchConfiguration {

    @Bean
    @Autowired
    @Qualifier("analyseStocks")
    public NightlyStockSymbolRecorder recorder(Job job) throws Throwable {
        return new NightlyStockSymbolRecorder(jobLauncher(), job);
    }

    @Bean
    public CloudEnvironment cloudEnvironment() {
        return new CloudEnvironment();
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer() {
        DataSourceInitializer dsi = new DataSourceInitializer();
        dsi.setDataSource(this.dataSource());
        ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
        String[] scripts = "/batch_psql.sql,/stocks_psql.sql".split(",");
        for (String s : scripts)
            resourceDatabasePopulator.addScript(new ClassPathResource(s));
        dsi.setDatabasePopulator(resourceDatabasePopulator);
        dsi.setEnabled(true);
        return dsi;
    }

    @Bean
    public DataSource dataSource() {

        Collection<RdbmsServiceInfo> servicesInfosForTheDbms = this.cloudEnvironment().getServiceInfos(RdbmsServiceInfo.class);
        Assert.isTrue(servicesInfosForTheDbms.size() > 0, "there must be at least one RDBMS bound!");
        RdbmsServiceInfo rdbmsServiceInfo = servicesInfosForTheDbms.iterator().next();
        RdbmsServiceCreator rdbmsServiceCreator = new RdbmsServiceCreator();
        return rdbmsServiceCreator.createService(rdbmsServiceInfo);
    }

    @Bean   // sets up infrastructure and scope
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() throws Exception {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(this.mapJobRegistry());
        return jobRegistryBeanPostProcessor;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    public MapJobRegistry mapJobRegistry() throws Exception {
        return new MapJobRegistry();
    }

    @Bean(name = "jobRepository")
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
        jobRepositoryFactoryBean.setDataSource(this.dataSource());
        jobRepositoryFactoryBean.setTransactionManager(this.transactionManager());
        jobRepositoryFactoryBean.afterPropertiesSet();
        return (JobRepository) jobRepositoryFactoryBean.getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
        simpleJobLauncher.setJobRepository(this.jobRepository());
        return simpleJobLauncher;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public StockSymbolLookupClient lookupClient() {
        return new YahooPipesQuotesApiStockSymbolLookupClient(restTemplate());
    }

    @Bean
    public ItemReader<String> reader() {

        RowMapper<String> rowMapper = new RowMapper<String>() {
            @Override
            public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getString("symbol");
            }
        };

        JdbcCursorItemReader<String> readerOfSymbols = new JdbcCursorItemReader<String>();
        readerOfSymbols.setSql("SELECT SYMBOL FROM STOCKS");
        readerOfSymbols.setRowMapper(rowMapper);
        readerOfSymbols.setDataSource(dataSource());
        return readerOfSymbols;
    }

    @Bean
    public ItemProcessor<String, StockSymbolLookup> processor() {
        final StockSymbolLookupClient stockSymbolLookupClient = this.lookupClient();
        return new ItemProcessor<String, StockSymbolLookup>() {
            @Override
            public StockSymbolLookup process(String tickerSymbol) throws Exception {
                try {
                    return stockSymbolLookupClient.lookupSymbol(tickerSymbol);
                } catch (Throwable throwable) {
                    throw new RuntimeException(throwable);
                }
            }
        };

    }

    @Bean(name = "writer")
    @Scope("step")
    public JdbcBatchItemWriter<StockSymbolLookup> writer(final @Value("#{jobParameters['date']}") Date dateOfAnalysis) {
        JdbcBatchItemWriter<StockSymbolLookup> jdbcBatchItemWriter = new JdbcBatchItemWriter<StockSymbolLookup>();
        jdbcBatchItemWriter.setSql("INSERT INTO STOCKS_DATA( DATE_ANALYSED, HIGH_PRICE, LOW_PRICE,  CLOSING_PRICE, SYMBOL) VALUES ( :da, :hp, :lp,  :cp, :s ) ");
        jdbcBatchItemWriter.setDataSource(this.dataSource());
        jdbcBatchItemWriter.setItemSqlParameterSourceProvider(new ItemSqlParameterSourceProvider<StockSymbolLookup>() {
            @Override
            public SqlParameterSource createSqlParameterSource(StockSymbolLookup item) {
                return new MapSqlParameterSource()
                        .addValue("da", new java.sql.Date(dateOfAnalysis.getTime()), Types.DATE)
                        .addValue("hp", item.getHighPrice(), Types.DOUBLE)
                        .addValue("lp", item.getLowPrice(), Types.DOUBLE)
                        .addValue("s", item.getTicker())
                        .addValue("si", item.getId(), Types.BIGINT)  // stock id has to be the same as in the Symbol lookup
                        .addValue("cp", item.getLastValueWhileOpen(), Types.DOUBLE);

            }
        });

        return jdbcBatchItemWriter;
    }


}
