package decider.job;

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
    Job job;

    @Test
    void testJobPath2() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("path", "2")
                .toJobParameters();


        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        assertEquals("main.jobs.decider.JobConfiguration", jobInstance.getJobName());
        assertEquals("COMPLETED", exitStatus.getExitCode());

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step1", stepExecution.getStepName());
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step2", stepExecution.getStepName());
        }

        // no more steps
        assertFalse(stepExecutionIterator.hasNext());

        assertEquals("step1", jobExecution.getExecutionContext().getString("step1"));
        assertEquals("step2", jobExecution.getExecutionContext().getString("step2"));
        assertNull(jobExecution.getExecutionContext().get("step3"));
        assertNull(jobExecution.getExecutionContext().get("step31"));
        assertNull(jobExecution.getExecutionContext().get("step4"));
        assertNull(jobExecution.getExecutionContext().get("step41"));
        assertNull(jobExecution.getExecutionContext().get("step42"));

    }

    @Test
    void testJobPath3() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("path", "3")
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        assertEquals("main.jobs.decider.JobConfiguration", jobInstance.getJobName());
        assertEquals("COMPLETED", exitStatus.getExitCode());

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step1", stepExecution.getStepName());
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step3", stepExecution.getStepName());
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step31", stepExecution.getStepName());
        }

        // no more steps
        assertFalse(stepExecutionIterator.hasNext());

        assertEquals("step1", jobExecution.getExecutionContext().getString("step1"));
        assertNull(jobExecution.getExecutionContext().get("step2"));
        assertEquals("step3", jobExecution.getExecutionContext().getString("step3"));
        assertEquals("step31", jobExecution.getExecutionContext().getString("step31"));
        assertNull(jobExecution.getExecutionContext().get("step4"));
        assertNull(jobExecution.getExecutionContext().get("step41"));
        assertNull(jobExecution.getExecutionContext().get("step42"));
    }

    @Test
    void testJobPath4() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("path", "4")
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        assertEquals("main.jobs.decider.JobConfiguration", jobInstance.getJobName());
        assertEquals("COMPLETED", exitStatus.getExitCode());

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step1", stepExecution.getStepName());
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step4", stepExecution.getStepName());
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step41", stepExecution.getStepName());
        }
        {
            StepExecution stepExecution = stepExecutionIterator.next();
            assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
            assertEquals("step42", stepExecution.getStepName());
        }

        // no more steps
        assertFalse(stepExecutionIterator.hasNext());

        assertEquals("step1", jobExecution.getExecutionContext().getString("step1"));
        assertNull(jobExecution.getExecutionContext().get("step2"));
        assertNull(jobExecution.getExecutionContext().get("step3"), "step3");
        assertNull(jobExecution.getExecutionContext().get("step31"), "step31");
        assertEquals("step4", jobExecution.getExecutionContext().getString("step4"));
        assertEquals("step41", jobExecution.getExecutionContext().getString("step41"));
        assertEquals("step42", jobExecution.getExecutionContext().getString("step42"));

    }

}
