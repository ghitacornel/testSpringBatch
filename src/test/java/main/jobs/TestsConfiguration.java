package main.jobs;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
@SpringBatchTest
public abstract class TestsConfiguration {

    @Autowired
    protected JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobLauncher jobLauncher;

    protected JobParameters defaultJobParameters() {
        JobParametersBuilder builder = new JobParametersBuilder();

        // at least 1 parameter that guarantees uniqueness of the job run
        builder.addLong("timestamp", System.currentTimeMillis());

        return builder.toJobParameters();
    }

}
