package tasklet.job;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Iterator;

@SpringBootTest
class JobConfigurationTest {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    @Test
    void testJob() throws Exception {

        JobExecution jobExecution = jobLauncher.run(job, new JobParameters());

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.tasklet.fails.JobConfiguration");
        Assertions.assertEquals(exitStatus.getExitCode(), "FAILED");

        Assertions.assertEquals(jobExecution.getFailureExceptions().size(), 0);

        Assertions.assertEquals(jobExecution.getAllFailureExceptions().size(), 1);
        Assertions.assertEquals(jobExecution.getAllFailureExceptions().getFirst().getMessage(), "step that must fail");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test stepFail
        StepExecution stepFail = stepExecutionIterator.next();
        Assertions.assertEquals(stepFail.getExitStatus().getExitCode(), ExitStatus.FAILED.getExitCode());
        Assertions.assertTrue(stepFail.getExitStatus().getExitDescription().startsWith("java.lang.RuntimeException: step that must fail"));
        Assertions.assertEquals(stepFail.getStepName(), "stepFail");
        Assertions.assertEquals(stepFail.getReadCount(), 1);
        Assertions.assertEquals(stepFail.getReadSkipCount(), 2);
        Assertions.assertEquals(stepFail.getWriteCount(), 3);
        Assertions.assertEquals(stepFail.getWriteSkipCount(), 4);
        Assertions.assertEquals(stepFail.getFilterCount(), 5);

        Assertions.assertEquals(stepFail.getSkipCount(), 12);// 2 reads skips + 4 writes skips + 6 process skips
        Assertions.assertEquals(stepFail.getCommitCount(), 0);

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

    }
}
