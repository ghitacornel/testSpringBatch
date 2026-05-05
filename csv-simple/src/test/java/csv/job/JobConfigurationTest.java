package csv.job;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
public class JobConfigurationTest {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    @TempDir
    Path workingFolder;

    @BeforeEach
    public void writeFile() throws IOException {
        Path input = Paths.get("src", "test", "resources", "input.csv");
        Path output = Paths.get(workingFolder.toString(), "input.csv");
        Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    public void checkFile() {
        Path input = Paths.get("src", "test", "resources", "output.csv");
        Path output = Paths.get(workingFolder.toString(), "output.csv");
        org.assertj.core.api.Assertions.assertThat(input.toFile()).hasSameTextualContentAs(output.toFile());
    }

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("inputPath", workingFolder.toString() + File.separator + "input.csv")
                .addString("outputPath", workingFolder.toString() + File.separator + "output.csv")
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        assertEquals("main.jobs.csv.JobConfiguration", jobInstance.getJobName());
        assertEquals("COMPLETED", exitStatus.getExitCode());

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        StepExecution stepExecution = stepExecutionIterator.next();
        assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
        assertEquals("main.jobs.csv.JobConfiguration.step", stepExecution.getStepName());
        assertEquals(1008, stepExecution.getReadCount());
        assertEquals(1000, stepExecution.getWriteCount());
        assertEquals(8, stepExecution.getFilterCount());
        assertEquals(11, stepExecution.getCommitCount());

        // no more steps
        assertFalse(stepExecutionIterator.hasNext());

    }
}
