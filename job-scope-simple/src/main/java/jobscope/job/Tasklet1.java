package jobscope.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class Tasklet1 implements Tasklet {

    private final BeanJobScoped beanJobScoped;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        beanJobScoped.step1 = true;
        return RepeatStatus.FINISHED;
    }
}
