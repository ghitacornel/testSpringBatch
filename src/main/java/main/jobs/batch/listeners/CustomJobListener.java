package main.jobs.batch.listeners;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class CustomJobListener extends JobExecutionListenerSupport {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        System.err.println(jobExecution);
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        System.err.println(jobExecution);
    }
}