package main.batch.listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class CustomStepListener implements StepExecutionListener {

    private static final Logger logger = LoggerFactory.getLogger(CustomStepListener.class);

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info(String.valueOf(stepExecution));
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        logger.info(String.valueOf(stepExecution));
        return stepExecution.getExitStatus();
    }

}
