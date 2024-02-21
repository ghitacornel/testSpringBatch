package jpa.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("main.jobs.jdbc.performance.JobJdbcReadWritePerformanceSingleThread")
class JobJpaReadWritePerformanceSingleThreadTest {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    @Test
    void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("count", 100000L)
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), JobJpaReadWritePerformanceSingleThread.JOB_NAME);
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

    }
}
