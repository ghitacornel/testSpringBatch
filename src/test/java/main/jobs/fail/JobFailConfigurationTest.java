package main.jobs.fail;

import main.jobs.TestsConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Iterator;

@ActiveProfiles("jobFail")
public class JobFailConfigurationTest extends TestsConfiguration {

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = defaultJobParameters();
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "jobFail");
        Assertions.assertEquals(exitStatus.getExitCode(), "FAILED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test stepFail
        StepExecution stepFail = stepExecutionIterator.next();
        Assertions.assertEquals(stepFail.getExitStatus().getExitCode(), ExitStatus.FAILED.getExitCode());
        Assertions.assertTrue(stepFail.getExitStatus().getExitDescription().startsWith("java.lang.RuntimeException: step that must fail"));
        Assertions.assertEquals(stepFail.getStepName(), "stepFail");
        Assertions.assertEquals(stepFail.getReadCount(), 0);
        Assertions.assertEquals(stepFail.getReadSkipCount(), 0);
        Assertions.assertEquals(stepFail.getWriteCount(), 0);
        Assertions.assertEquals(stepFail.getWriteSkipCount(), 0);
        Assertions.assertEquals(stepFail.getFilterCount(), 0);
        Assertions.assertEquals(stepFail.getSkipCount(), 0);
        Assertions.assertEquals(stepFail.getCommitCount(), 0);

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

    }
}
