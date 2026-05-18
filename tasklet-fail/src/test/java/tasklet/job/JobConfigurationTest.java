package tasklet.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

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

        assertEquals("main.jobs.tasklet.fails.JobConfiguration", jobInstance.getJobName());
        assertEquals("FAILED", exitStatus.getExitCode());

        assertEquals(0, jobExecution.getFailureExceptions().size());

        assertEquals(1, jobExecution.getAllFailureExceptions().size());
        assertEquals("step that must fail", jobExecution.getAllFailureExceptions().getFirst().getMessage());

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test stepFail
        StepExecution stepFail = stepExecutionIterator.next();
        assertEquals(stepFail.getExitStatus().getExitCode(), ExitStatus.FAILED.getExitCode());
        assertTrue(stepFail.getExitStatus().getExitDescription().startsWith("java.lang.RuntimeException: step that must fail"));
        assertEquals("stepFail", stepFail.getStepName());
        assertEquals(1, stepFail.getReadCount());
        assertEquals(2, stepFail.getReadSkipCount());
        assertEquals(3, stepFail.getWriteCount());
        assertEquals(4, stepFail.getWriteSkipCount());
        assertEquals(5, stepFail.getFilterCount());

        assertEquals(12, stepFail.getSkipCount());// 2 reads skips + 4 writes skips + 6 process skips
        assertEquals(0, stepFail.getCommitCount());

        // no more steps
        assertFalse(stepExecutionIterator.hasNext());

    }
}
