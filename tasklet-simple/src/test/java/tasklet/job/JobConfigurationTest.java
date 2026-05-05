package tasklet.job;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JobConfigurationTest {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    CustomJobExecutionListener jobExecutionListener;

    @Autowired
    CustomStepExecutionListener stepExecutionListener;

    @Autowired
    Job job;

    @Test
    void testJob() throws Exception {

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters());

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        assertEquals(jobInstance.getJobName(), job.getName());
        assertEquals("COMPLETED", exitStatus.getExitCode());

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test singleExecutionStep
        StepExecution singleExecutionStep = stepExecutionIterator.next();
        assertEquals(ExitStatus.COMPLETED, singleExecutionStep.getExitStatus());
        assertEquals("singleExecutionStep", singleExecutionStep.getStepName());
        assertEquals(0, singleExecutionStep.getReadCount());
        assertEquals(0, singleExecutionStep.getReadSkipCount());
        assertEquals(0, singleExecutionStep.getWriteCount());
        assertEquals(0, singleExecutionStep.getWriteSkipCount());
        assertEquals(0, singleExecutionStep.getFilterCount());
        assertEquals(0, singleExecutionStep.getSkipCount());
        assertEquals(1, singleExecutionStep.getCommitCount());

        // test repeatableExecutionStep
        StepExecution repeatableExecutionStep = stepExecutionIterator.next();
        assertEquals(ExitStatus.COMPLETED, repeatableExecutionStep.getExitStatus());
        assertEquals("repeatableExecutionStep", repeatableExecutionStep.getStepName());
        assertEquals(0, repeatableExecutionStep.getReadCount());
        assertEquals(0, repeatableExecutionStep.getReadSkipCount());
        assertEquals(0, repeatableExecutionStep.getWriteCount());
        assertEquals(0, repeatableExecutionStep.getWriteSkipCount());
        assertEquals(0, repeatableExecutionStep.getFilterCount());
        assertEquals(0, repeatableExecutionStep.getSkipCount());
        assertEquals(4, repeatableExecutionStep.getCommitCount());

        // no more steps
        assertFalse(stepExecutionIterator.hasNext());

        // check listeners
        assertTrue(jobExecutionListener.beforeExecuted);
        assertTrue(jobExecutionListener.afterExecuted);
        assertTrue(stepExecutionListener.beforeExecuted);
        assertTrue(stepExecutionListener.afterExecuted);

    }

}
