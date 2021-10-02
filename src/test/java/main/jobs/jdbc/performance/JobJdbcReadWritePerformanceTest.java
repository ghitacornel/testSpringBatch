package main.jobs.jdbc.performance;

import main.jobs.TestsConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("main.jobs.jdbc.performance.JobJdbcReadWritePerformance")
public class JobJdbcReadWritePerformanceTest extends TestsConfiguration {

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("count", 1000000L)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.jdbc.performance.JobJdbcReadWritePerformance");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

    }
}
