package main.jobs.csvs;

import main.jobs.TestsConfiguration;
import main.jobs.tasklets.JobTaskletExecutionListener;
import main.jobs.tasklets.JobTaskletStepExecutionListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.test.AssertFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Iterator;

@ActiveProfiles("main.jobs.csvs.Job1ReadWrite")
public class Job1ReadWriteTest extends TestsConfiguration {

    @Autowired
    JobTaskletExecutionListener job1ExecutionListener;

    @Autowired
    JobTaskletStepExecutionListener jobTaskletStepExecutionListener;

    @TempDir
    Path workingFolder;

    @BeforeEach
    public void writeFile() throws IOException {
        Path input = Paths.get("src", "test", "resources", "csvs", "input.csv");
        Path output = Paths.get(workingFolder.toString(), "input.csv");
        Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    public void checkFile() throws Exception {
        Path input = Paths.get("src", "test", "resources", "csvs", "output.csv");
        Path output = Paths.get(workingFolder.toString(), "output.csv");
        AssertFile.assertFileEquals(input.toFile(), output.toFile());
    }

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("inputPath", workingFolder.toString() + File.separator + "input.csv")
                .addString("outputPath", workingFolder.toString() + File.separator + "output.csv")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.csvs.Job1ReadWrite");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        StepExecution stepExecution = stepExecutionIterator.next();
        Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(stepExecution.getStepName(), "main.jobs.csvs.Job1ReadWrite.step");

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

    }
}
