package jobscope.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@Component
@JobScope
@RequiredArgsConstructor
public class JobListener implements JobExecutionListener {

    private final BeanJobScoped beanJobScoped;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        beanJobScoped.startJob = true;
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        beanJobScoped.endJob = true;

        // all checks go here
        if (!beanJobScoped.startJob) throw new RuntimeException("startJob false");
        if (!beanJobScoped.startStep1) throw new RuntimeException("startStep1 false");
        if (!beanJobScoped.step1) throw new RuntimeException("step1 false");
        if (!beanJobScoped.endStep1) throw new RuntimeException("endStep1 false");
        if (!beanJobScoped.startStep2) throw new RuntimeException("startStep2 false");
        if (!beanJobScoped.step2) throw new RuntimeException("step2 false");
        if (!beanJobScoped.endStep2) throw new RuntimeException("endStep2 false");
        if (!beanJobScoped.endJob) throw new RuntimeException("endJob false");
    }
}
