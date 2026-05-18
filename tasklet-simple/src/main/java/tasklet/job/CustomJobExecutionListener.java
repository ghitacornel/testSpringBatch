package tasklet.job;

import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class CustomJobExecutionListener implements JobExecutionListener {

    boolean beforeExecuted;
    boolean afterExecuted;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        beforeExecuted = true;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        afterExecuted = true;
    }

}
