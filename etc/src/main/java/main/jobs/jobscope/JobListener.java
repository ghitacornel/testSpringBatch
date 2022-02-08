package main.jobs.jobscope;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.stereotype.Component;

@Component
@JobScope
public class JobListener implements javax.batch.api.listener.JobListener {

    final BeanJobScoped beanJobScoped;

    public JobListener(BeanJobScoped beanJobScoped) {
        this.beanJobScoped = beanJobScoped;
    }

    @Override
    public void beforeJob() {
        beanJobScoped.startJob = true;
    }

    @Override
    public void afterJob() {
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
