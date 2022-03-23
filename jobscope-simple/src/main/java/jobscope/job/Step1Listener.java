package jobscope.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class Step1Listener implements StepExecutionListener {

    private final BeanJobScoped beanJobScoped;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        beanJobScoped.startStep1 = true;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        beanJobScoped.endStep1 = true;
        return null;
    }
}
