package jpa.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class JobJpaReadWriteValidateTest {

    @Autowired
    JobLauncher jobLauncher;

    @Qualifier("jobJpaReadWriteValidate")
    @Autowired
    Job job;

    @Test
    void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("count", 10000L)
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "jobJpaReadWriteValidate");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

    }
}
