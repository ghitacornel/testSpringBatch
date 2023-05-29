package tasklet.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job(JobTaskletExecutionListener job1ExecutionListener, JobTaskletStepExecutionListener jobTaskletStepExecutionListener) {
        return jobBuilderFactory.get("main.jobs.tasklet.JobConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(step1(jobTaskletStepExecutionListener))
                .next(step2())
                .listener(job1ExecutionListener)
                .build();
    }

    Step step1(JobTaskletStepExecutionListener jobTaskletStepExecutionListener) {
        return stepBuilderFactory
                .get("singleExecutionStep")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("single execution step");
                    return RepeatStatus.FINISHED;
                })
                .listener(jobTaskletStepExecutionListener)
                .build();
    }

    Step step2() {
        return stepBuilderFactory
                .get("repeatableExecutionStep")
                .tasklet((contribution, chunkContext) -> {
                    Integer attribute = (Integer) chunkContext.getAttribute("counts");
                    if (attribute == null) attribute = 0;
                    if (attribute < 3) {
                        attribute++;
                        chunkContext.setAttribute("counts", attribute);
                        System.out.println("repeatableExecutionStep executed " + attribute);
                        return RepeatStatus.CONTINUABLE;
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
