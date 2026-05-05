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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JobConfigurationTest {

    @Autowired
    JobLauncher jobLauncher;

    @Autowired
    Job job;

    @TempDir
    Path workingFolder;

    @BeforeEach
    void writeFile() throws IOException {
        Path input = Paths.get("src", "test", "resources", "input.csv");
        Path output = Paths.get(workingFolder.toString(), "input.csv");
        Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterEach
    void checkFile() throws Exception {
        Path input = Paths.get(workingFolder.toString(), "input.csv");
        List<String> inputIds = new ArrayList<>();
        for (String line : Files.readAllLines(input)) {
            inputIds.add(line.split(",")[0]);
        }
        Path output = Paths.get(workingFolder.toString(), "output.csv");
        List<String> outputIds = new ArrayList<>();
        for (String line : Files.readAllLines(output)) {
            outputIds.add(line.split(",")[0]);
        }
        assertTrue(output.toFile().exists());
        assertTrue(inputIds.containsAll(outputIds));
        outputIds.add("id");
        outputIds.add("5");
        outputIds.add("10");
        outputIds.add("15");
        outputIds.add("20");
        outputIds.add("25");
        assertTrue(inputIds.containsAll(outputIds));
        assertTrue(outputIds.containsAll(inputIds));
    }

    @Test
    void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addString("inputPath", workingFolder.toString() + File.separator + "input.csv")
                .addString("outputPath", workingFolder.toString() + File.separator + "output.csv")
                .toJobParameters();

        JobExecution jobExecution = jobLauncher.run(job, jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        assertEquals("main.jobs.csv.parallel.JobConfiguration", jobInstance.getJobName());
        assertEquals("COMPLETED", exitStatus.getExitCode());

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        StepExecution stepExecution = stepExecutionIterator.next();
        assertEquals(ExitStatus.COMPLETED, stepExecution.getExitStatus());
        assertEquals("main.jobs.csv.parallel.JobConfiguration.step", stepExecution.getStepName());
        assertEquals(1000, stepExecution.getReadCount());
        assertEquals(995, stepExecution.getWriteCount());
        assertEquals(5, stepExecution.getFilterCount());
        assertEquals(104, stepExecution.getCommitCount());

        // no more steps
        assertFalse(stepExecutionIterator.hasNext());

    }
}
