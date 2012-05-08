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

import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Simple example of a jl that runs in a background thread and does some
 * long-running work (in this case, looking up stock information)
 *
 * @author Josh Long (josh.long@springsource.com)
 */
public class NightlyStockSymbolRecorder {

    private Logger logger = Logger.getLogger(getClass().getName());

    private JobLauncher jobLauncher;
    private Job job;

    // @Scheduled(cron = "0 23 ? * MON-FRI")
    // every weekday at 11 PM / 23h00

    // set to every 10s for testing.
    @Scheduled(fixedRate = 10 * 1000)
    public void runNightlyStockPriceRecorder() throws Throwable {
        JobParameters params = new JobParametersBuilder()
                .addDate("date", new Date())
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, params);
        BatchStatus batchStatus = jobExecution.getStatus();
        while (batchStatus.isRunning()) {
            logger.info("Still running...");
            Thread.sleep(1000);
        }
        logger.info(String.format("Exit status: %s", jobExecution.getExitStatus().getExitCode()));
        JobInstance jobInstance = jobExecution.getJobInstance();
        logger.info(String.format("job instance Id: %d", jobInstance.getId()));
    }

    public NightlyStockSymbolRecorder(JobLauncher jl, Job jobToRun) {
        this.jobLauncher = jl;
        this.job = jobToRun;
    }


}
