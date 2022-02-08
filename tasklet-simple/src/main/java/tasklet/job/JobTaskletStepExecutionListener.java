package tasklet.job;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

@Component
public class JobTaskletStepExecutionListener implements StepExecutionListener {

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
