package tasklet.job.common;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@SpringBatchTest
public abstract class TestsConfiguration {

    @Autowired
    protected JobLauncher jobLauncher;

}
