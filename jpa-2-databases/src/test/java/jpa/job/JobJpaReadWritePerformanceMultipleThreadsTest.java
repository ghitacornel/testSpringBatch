package jpa.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

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

        assertEquals("jobJpaReadWritePerformanceMultipleThreads", jobInstance.getJobName());
        assertEquals("COMPLETED", exitStatus.getExitCode());

    }
}
