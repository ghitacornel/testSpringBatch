package tasklet.job;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.listener.StepExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class CustomStepExecutionListener implements StepExecutionListener {

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
