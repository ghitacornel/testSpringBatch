package main.jobs.job1;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class Job1ExecutionListener implements JobExecutionListener {

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