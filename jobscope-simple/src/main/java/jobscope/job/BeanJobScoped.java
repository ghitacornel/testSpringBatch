package jobscope.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@Component
@JobScope
@RequiredArgsConstructor
public class BeanJobScoped {

    private final JobExecution jobExecution;

    boolean startJob;
    boolean startStep1;
    boolean step1;
    boolean endStep1;
    boolean startStep2;
    boolean step2;
    boolean endStep2;
    boolean endJob;

}
