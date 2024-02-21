package jobscope.job;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@Component
@JobScope
public class BeanJobScoped {

    boolean startJob;
    boolean startStep1;
    boolean step1;
    boolean endStep1;
    boolean startStep2;
    boolean step2;
    boolean endStep2;
    boolean endJob;

}
