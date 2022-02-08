package jobscope.job;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
class Tasklet1 implements Tasklet {

    final BeanJobScoped beanJobScoped;

    public Tasklet1(BeanJobScoped beanJobScoped) {
        this.beanJobScoped = beanJobScoped;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        beanJobScoped.step1 = true;
        return RepeatStatus.FINISHED;
    }
}
