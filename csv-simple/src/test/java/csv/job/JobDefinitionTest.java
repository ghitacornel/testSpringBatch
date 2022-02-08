package csv.job;

import csv.job.common.TestsConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.test.AssertFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

public class JobDefinitionTest extends TestsConfiguration {

    @TempDir
    Path workingFolder;

    @BeforeEach
    public void writeFile() throws IOException {
        Path input = Paths.get("src", "test", "resources", "csv", "input.csv");
        Path output = Paths.get(workingFolder.toString(), "input.csv");
        Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    public void checkFile() throws Exception {
        Path input = Paths.get("src", "test", "resources", "csv", "output.csv");
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

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.csv.JobDefinition");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        StepExecution stepExecution = stepExecutionIterator.next();
        Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(stepExecution.getStepName(), "main.jobs.csv.JobDefinition.step");
        Assertions.assertEquals(stepExecution.getReadCount(), 1008);
        Assertions.assertEquals(stepExecution.getWriteCount(), 1000);
        Assertions.assertEquals(stepExecution.getFilterCount(), 8);
        Assertions.assertEquals(stepExecution.getCommitCount(), 11);

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

    }
}
