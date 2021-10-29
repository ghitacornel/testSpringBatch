package main.jobs.jobscope;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
class Tasklet1 implements Tasklet {

    @Autowired
    BeanJobScoped beanJobScoped;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        beanJobScoped.step1 = true;
        return RepeatStatus.FINISHED;
    }
}
