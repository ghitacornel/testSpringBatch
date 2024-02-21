package csv.job;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
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
        Assertions.assertTrue(output.toFile().exists());
        Assertions.assertTrue(inputIds.containsAll(outputIds));
        outputIds.add("id");
        outputIds.add("5");
        outputIds.add("10");
        outputIds.add("15");
        outputIds.add("20");
        outputIds.add("25");
        Assertions.assertTrue(inputIds.containsAll(outputIds));
        Assertions.assertTrue(outputIds.containsAll(inputIds));
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

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.csv.parallel.JobConfiguration");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

        Iterator<StepExecution> stepExecutionIterator = jobExecution.getStepExecutions().iterator();

        // test step
        StepExecution stepExecution = stepExecutionIterator.next();
        Assertions.assertEquals(stepExecution.getExitStatus(), ExitStatus.COMPLETED);
        Assertions.assertEquals(stepExecution.getStepName(), "main.jobs.csv.parallel.JobConfiguration.step");
        Assertions.assertEquals(stepExecution.getReadCount(), 1000);
        Assertions.assertEquals(stepExecution.getWriteCount(), 995);
        Assertions.assertEquals(stepExecution.getFilterCount(), 5);
        Assertions.assertEquals(stepExecution.getCommitCount(), 104);

        // no more steps
        Assertions.assertFalse(stepExecutionIterator.hasNext());

    }
}
