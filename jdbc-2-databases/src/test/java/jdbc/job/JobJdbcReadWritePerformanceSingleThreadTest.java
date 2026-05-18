package jdbc.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class JobJdbcReadWritePerformanceSingleThreadTest {

    @Autowired
    JobOperator jobOperator;

    @Qualifier("jobJdbcReadWritePerformanceSingleThread")
    @Autowired
    Job job;

    @Test
    void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("count", 100000L)
                .toJobParameters();

        JobExecution jobExecution = jobOperator.start(job, jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        assertEquals("jobJdbcReadWritePerformanceSingleThread", jobInstance.getJobName());
        assertEquals("COMPLETED", exitStatus.getExitCode());

    }
}
