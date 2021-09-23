package main.jobs.job1;

import main.jobs.TestsConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;

public class Job1ConfigurationTest extends TestsConfiguration {

    @Autowired
    Job1ExecutionListener job1ExecutionListener;

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = defaultJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "job1");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test singleExecutionStep
        StepExecution singleExecutionStep = stepExecutionIterator.next();
        Assertions.assertEquals(singleExecutionStep.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(singleExecutionStep.getStepName(), "singleExecutionStep");
        Assertions.assertEquals(singleExecutionStep.getReadCount(), 0);
        Assertions.assertEquals(singleExecutionStep.getWriteCount(), 0);

        // test repeatableExecutionStep
        StepExecution repeatableExecutionStep = stepExecutionIterator.next();
        Assertions.assertEquals(repeatableExecutionStep.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(repeatableExecutionStep.getStepName(), "repeatableExecutionStep");

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

        // check listeners
        Assertions.assertTrue(job1ExecutionListener.beforeExecuted);
        Assertions.assertTrue(job1ExecutionListener.afterExecuted);

    }
}
