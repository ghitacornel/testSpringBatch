package main.jobs.decider;

import main.jobs.TestsConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Iterator;

@ActiveProfiles("main.jobs.decider.JobDeciderConfiguration")
public class JobDeciderTest extends TestsConfiguration {

    @Test
    public void testJobPath2() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("path", "2")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.decider.JobDeciderConfiguration");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step1");
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step2");
        }

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step1"), "step1");
        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step2"), "step2");
        Assertions.assertNull(jobExecution.getExecutionContext().get("step3"));
        Assertions.assertNull(jobExecution.getExecutionContext().get("step31"));
        Assertions.assertNull(jobExecution.getExecutionContext().get("step4"));
        Assertions.assertNull(jobExecution.getExecutionContext().get("step41"));
        Assertions.assertNull(jobExecution.getExecutionContext().get("step42"));

    }

    @Test
    public void testJobPath3() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("path", "3")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.decider.JobDeciderConfiguration");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step1");
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step3");
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step31");
        }

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step1"), "step1");
        Assertions.assertNull(jobExecution.getExecutionContext().get("step2"));
        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step3"), "step3");
        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step31"), "step31");
        Assertions.assertNull(jobExecution.getExecutionContext().get("step4"));
        Assertions.assertNull(jobExecution.getExecutionContext().get("step41"));
        Assertions.assertNull(jobExecution.getExecutionContext().get("step42"));
    }

    @Test
    public void testJobPath4() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("path", "4")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.decider.JobDeciderConfiguration");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step1");
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step4");
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step41");
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
            Assertions.assertEquals(stepExecution.getStepName(), "step42");
        }

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step1"), "step1");
        Assertions.assertNull(jobExecution.getExecutionContext().get("step2"));
        Assertions.assertNull(jobExecution.getExecutionContext().get("step3"), "step3");
        Assertions.assertNull(jobExecution.getExecutionContext().get("step31"), "step31");
        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step4"), "step4");
        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step41"), "step41");
        Assertions.assertEquals(jobExecution.getExecutionContext().getString("step42"), "step42");

    }
}
