package jdbc.job;

import jdbc.job.jdbc.common.TestsConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("main.jobs.jdbc.performance.JobJdbcReadWritePerformanceMultipleThreads")
public class JobJpaReadWritePerformanceMultipleThreadsTest extends TestsConfiguration {

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("count", 100000L)
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), JobJpaReadWritePerformanceMultipleThreads.JOB_NAME);
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

    }
}
