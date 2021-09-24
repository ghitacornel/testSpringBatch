package main.jobs.tasklets;

import org.springframework.batch.core.*;
import org.springframework.stereotype.Component;

@Component
public class Job1StepExecutionListener implements StepExecutionListener {

    boolean beforeExecuted;
    boolean afterExecuted;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        beforeExecuted = true;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        afterExecuted = true;

        // can override step execution status here
        return stepExecution.getExitStatus();
    }
}
