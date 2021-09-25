package main.jobs.tasklet;

import main.jobs.TestsConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.util.Iterator;

@ActiveProfiles("main.jobs.tasklet.JobTaskletConfiguration")
public class JobTaskletConfigurationTest extends TestsConfiguration {

    @Autowired
    JobTaskletExecutionListener jobExecutionListener;

    @Autowired
    JobTaskletStepExecutionListener stepExecutionListener;

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = defaultJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.tasklet.JobTaskletConfiguration");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test singleExecutionStep
        StepExecution singleExecutionStep = stepExecutionIterator.next();
        Assertions.assertEquals(singleExecutionStep.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(singleExecutionStep.getStepName(), "singleExecutionStep");
        Assertions.assertEquals(singleExecutionStep.getReadCount(), 0);
        Assertions.assertEquals(singleExecutionStep.getReadSkipCount(), 0);
        Assertions.assertEquals(singleExecutionStep.getWriteCount(), 0);
        Assertions.assertEquals(singleExecutionStep.getWriteSkipCount(), 0);
        Assertions.assertEquals(singleExecutionStep.getFilterCount(), 0);
        Assertions.assertEquals(singleExecutionStep.getSkipCount(), 0);
        Assertions.assertEquals(singleExecutionStep.getCommitCount(), 1);

        // test repeatableExecutionStep
        StepExecution repeatableExecutionStep = stepExecutionIterator.next();
        Assertions.assertEquals(repeatableExecutionStep.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(repeatableExecutionStep.getStepName(), "repeatableExecutionStep");
        Assertions.assertEquals(repeatableExecutionStep.getReadCount(), 0);
        Assertions.assertEquals(repeatableExecutionStep.getReadSkipCount(), 0);
        Assertions.assertEquals(repeatableExecutionStep.getWriteCount(), 0);
        Assertions.assertEquals(repeatableExecutionStep.getWriteSkipCount(), 0);
        Assertions.assertEquals(repeatableExecutionStep.getFilterCount(), 0);
        Assertions.assertEquals(repeatableExecutionStep.getSkipCount(), 0);
        Assertions.assertEquals(repeatableExecutionStep.getCommitCount(), 4);

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

        // check listeners
        Assertions.assertTrue(jobExecutionListener.beforeExecuted);
        Assertions.assertTrue(jobExecutionListener.afterExecuted);
        Assertions.assertTrue(stepExecutionListener.beforeExecuted);
        Assertions.assertTrue(stepExecutionListener.afterExecuted);

    }
}