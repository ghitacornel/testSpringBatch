package jpa.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JobJpaReadWritePerformanceMultipleThreadsTest {

    @Autowired
    JobLauncher jobLauncher;

    @Qualifier("jobJpaReadWritePerformanceMultipleThreads")
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

        Assertions.assertEquals(jobInstance.getJobName(), "jobJpaReadWritePerformanceMultipleThreads");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

    }
}
