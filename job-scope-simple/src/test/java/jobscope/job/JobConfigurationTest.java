package jobscope.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class JobConfigurationTest {

    @Autowired
    JobOperator jobOperator;

    @Autowired
    Job job;

    @Test
    void testJob() throws Exception {

        JobExecution jobExecution = jobOperator.start(job, new JobParameters());

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        assertEquals("main.jobs.jobscope.JobConfiguration", jobInstance.getJobName());
        assertEquals("COMPLETED", exitStatus.getExitCode());
    }
}
