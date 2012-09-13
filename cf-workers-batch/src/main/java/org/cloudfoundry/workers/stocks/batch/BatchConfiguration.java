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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.cloudfoundry.runtime.env.CloudEnvironment;
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
import org.springframework.context.annotation.Import;
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
import org.springframework.web.client.RestTemplate;

/**
 * Configures a Spring Batch job that looks at the ticker symbols in the
 * 'STOCKS' table, and then retrieves the information for all of the records for
 * that day.
 *
 * @author Josh Long (josh.long@springsource.com)
 * @author Oleg Zhurakousky
 */
@Configuration
// tells Spring that this is a Java-centric configuration class
@ImportResource("/batch.xml")
@Import ({LocalDataSourceConfiguration.class, CloudDataSourceConfiguration.class})
// contains the Spring Batch DSL which in turn sets up the Batch job
@EnableScheduling
// to enable the use of the @Scheduled annotation
public class BatchConfiguration {

	@Inject
	private DataSourceConfiguration dsConfig;

	/**
	 *
	 * The job that we want to run on a schedule (as made possible with Spring's
	 * {@link org.springframework.scheduling.annotation.Scheduled scheduled
	 * annotation}.)
	 *
	 * @throws Throwable
	 */
	@Bean
	@Autowired
	@Qualifier("analyseStocks")
	public NightlyStockSymbolRecorder recorder(Job job) throws Throwable {
		return new NightlyStockSymbolRecorder(jobLauncher(), job);
	}

	/**
	 * The Cloud Foundry runtime provides this object, which serves as a
	 * connection between the client and Cloud Foundry runtime. It knows how to
	 * ask questions of the cloud, and how to extract resources like
	 * {@link DataSource data sources}.
	 */
	@Bean
	public CloudEnvironment cloudEnvironment() {
		return new CloudEnvironment();
	}

	/**
	 * We have to have certain records and certain tables for our application to
	 * work correctly. This object will be used to ensure that certain
	 * <CODE>sql</CODE> files are executed on application startup.
	 *
	 * TODO make the SQL a little smarter about not recreating the tables unless
	 * they don't exist.
	 *
	 */
	//@Bean
	public DataSourceInitializer dataSourceInitializer() {
		DataSourceInitializer dsi = new DataSourceInitializer();
		dsi.setDataSource(dsConfig.dataSource());
		ResourceDatabasePopulator resourceDatabasePopulator = new ResourceDatabasePopulator();
		String[] scripts = "/batch_psql.sql,/stocks_psql.sql".split(",");
		for (String s : scripts)
			resourceDatabasePopulator.addScript(new ClassPathResource(s));
		dsi.setDatabasePopulator(resourceDatabasePopulator);
		dsi.setEnabled(true);
		return dsi;
	}

	/**
	 * this bean registers Spring Batch {@link Job} instances with the runtime
	 *
	 * @throws Exception
	 */
	@Bean
	// sets up infrastructure and scope
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor()
			throws Exception {
		JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
		jobRegistryBeanPostProcessor.setJobRegistry(this.mapJobRegistry());
		return jobRegistryBeanPostProcessor;
	}

	/**
	 * We're working with a transactional RDBMS, so this implementation of
	 * Spring's {@link PlatformTransactionManager} API is required in certain
	 * cases.
	 */
	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dsConfig.dataSource());
	}

	/**
	 * Registers Spring Batch {@link Job jobs} with the
	 * {@link JobRegistryBeanPostProcessor}
	 *
	 * @see JobRegistryBeanPostProcessor
	 * @throws Exception
	 */
	@Bean
	public MapJobRegistry mapJobRegistry() throws Exception {
		return new MapJobRegistry();
	}

	/**
	 * Stores information about the {@link Job jobs} into a backend store (like
	 * a {@link DataSource})
	 *
	 * @throws Exception
	 */
	@Bean
	public JobRepository jobRepository() throws Exception {
		JobRepositoryFactoryBean jobRepositoryFactoryBean = new JobRepositoryFactoryBean();
		jobRepositoryFactoryBean.setDataSource(dsConfig.dataSource());
		jobRepositoryFactoryBean.setTransactionManager(this
				.transactionManager());
		jobRepositoryFactoryBean.afterPropertiesSet();
		return (JobRepository) jobRepositoryFactoryBean.getObject();
	}

	/**
	 * Used for launching Spring Batch {@link Job job} instances
	 *
	 * @throws Exception
	 */
	@Bean
	public SimpleJobLauncher jobLauncher() throws Exception {
		SimpleJobLauncher simpleJobLauncher = new SimpleJobLauncher();
		simpleJobLauncher.setJobRepository(this.jobRepository());
		return simpleJobLauncher;
	}

	/**
	 * Used for working with RESTful resources. We can configure custom
	 * {@link org.springframework.http.converter.HttpMessageConverter message
	 * converters}, or other interesting objects. Because we don't, in this
	 * case, there's little reason this needs to be constructed inside of the
	 * Spring container. That said, it doesn't hurt.
	 */
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	/**
	 * an implementation of the {@link StockSymbolLookupClient stock symbol
	 * lookup client}. There are currently three implementations. I originally
	 * had a {@link GoogleFinanceStockSymbolLookupClient Google Finance
	 * implementation}. At one point I was offline, so I needed a (
	 * {@link org.cloudfoundry.workers.stocks.MockStockSymbolLookupClient mock
	 * version}. The Google Finance one eventually stopped working (they started
	 * blocking me) so I created one based on Yahoo!'s YQL platform, which seems
	 * both more convenient (well documented) and more enduring (I think they
	 * actually want people to use their APIs).
	 */
	@Bean
	public StockSymbolLookupClient lookupClient() {
		return new YahooPipesQuotesApiStockSymbolLookupClient(restTemplate());
	}

	/**
	 * The Spring Batch {@link ItemReader item reader implementation} looks up
	 * which stock symbols need to be searched from the <CODE>STOCKS</CODE>
	 * table.
	 */
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
		readerOfSymbols.setDataSource(dsConfig.dataSource());
		return readerOfSymbols;
	}

	/**
	 * The Spring Batch {@link ItemProcessor item processor} takes the stocks
	 * from the reader and converts them into {@link StockSymbolLookup stock
	 * symbol lookups}, which it then passes to the writer to persist.
	 */
	@Bean
	public ItemProcessor<String, StockSymbolLookup> processor() {
		final StockSymbolLookupClient stockSymbolLookupClient = this
				.lookupClient();
		return new ItemProcessor<String, StockSymbolLookup>() {
			@Override
			public StockSymbolLookup process(String tickerSymbol)
					throws Exception {
				try {
					return stockSymbolLookupClient.lookupSymbol(tickerSymbol);
				} catch (Throwable throwable) {
					throw new RuntimeException(throwable);
				}
			}
		};

	}

	/***
	 * The Spring Batch {@link org.springframework.batch.item.ItemWriter item
	 * writer} persists the {@link StockSymbolLookup} lookup results into a
	 * table (<CODE>STOCKS_DATA</CODE>).
	 *
	 * @param dateOfAnalysis
	 */
	@Bean(name = "writer")
	@Scope("step")
	public JdbcBatchItemWriter<StockSymbolLookup> writer(
			final @Value("#{jobParameters['date']}") Date dateOfAnalysis) {
		JdbcBatchItemWriter<StockSymbolLookup> jdbcBatchItemWriter = new JdbcBatchItemWriter<StockSymbolLookup>();
		jdbcBatchItemWriter
				.setSql("INSERT INTO STOCKS_DATA( DATE_ANALYSED, HIGH_PRICE, LOW_PRICE,  CLOSING_PRICE, SYMBOL) VALUES ( :da, :hp, :lp,  :cp, :s ) ");
		jdbcBatchItemWriter.setDataSource(dsConfig.dataSource());
		jdbcBatchItemWriter
				.setItemSqlParameterSourceProvider(new ItemSqlParameterSourceProvider<StockSymbolLookup>() {
					@Override
					public SqlParameterSource createSqlParameterSource(
							StockSymbolLookup item) {
						return new MapSqlParameterSource()
								.addValue(
										"da",
										new java.sql.Date(dateOfAnalysis
												.getTime()), Types.DATE)
								.addValue("hp", item.getHighPrice(),
										Types.DOUBLE)
								.addValue("lp", item.getLowPrice(),
										Types.DOUBLE)
								.addValue("s", item.getTicker())
								.addValue("si", item.getId(), Types.BIGINT) // stock
																			// id
																			// has
																			// to
																			// be
																			// the
																			// same
																			// as
																			// in
																			// the
																			// Symbol
																			// lookup
								.addValue("cp", item.getLastValueWhileOpen(),
										Types.DOUBLE);

					}
				});
		return jdbcBatchItemWriter;
	}

}
