package main.jobs.csv.performance;

import main.jobs.TestsConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.batch.core.*;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.nio.file.Path;

@ActiveProfiles("main.jobs.csv.performance.JobCsvReadWritePerformance")
public class JobCsvReadWritePerformanceTest extends TestsConfiguration {

    @TempDir
    Path workingFolder;

    @Test
    public void testJob() throws Exception {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .addLong("count", 100000L)
                .addString("inputPath", workingFolder.toString() + File.separator + "input.csv")
                .addString("outputPath", workingFolder.toString() + File.separator + "output.csv")
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        JobInstance jobInstance = jobExecution.getJobInstance();
        ExitStatus exitStatus = jobExecution.getExitStatus();

        Assertions.assertEquals(jobInstance.getJobName(), "main.jobs.csv.performance.JobCsvReadWritePerformance");
        Assertions.assertEquals(exitStatus.getExitCode(), "COMPLETED");

    }
}
