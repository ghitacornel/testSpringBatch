package main.jobs.jobscope;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
class Tasklet2 implements Tasklet {

    final BeanJobScoped beanJobScoped;

    public Tasklet2(BeanJobScoped beanJobScoped) {
        this.beanJobScoped = beanJobScoped;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        beanJobScoped.step2 = true;
        return RepeatStatus.FINISHED;
    }
}
