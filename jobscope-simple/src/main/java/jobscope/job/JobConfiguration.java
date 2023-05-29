package jobscope.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job(Step step1, Step step2, JobListener jobListener) {
        return jobBuilderFactory.get("main.jobs.jobscope.JobConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .listener(jobListener)
                .build();
    }

    @Bean
    Step step1(Tasklet1 tasklet, Step1Listener listener) {
        return stepBuilderFactory
                .get("step1")
                .tasklet(tasklet)
                .listener(listener)
                .build();
    }

    @Bean
    Step step2(Tasklet2 tasklet, Step2Listener listener) {
        return stepBuilderFactory
                .get("step2")
                .tasklet(tasklet)
                .listener(listener)
                .build();
    }

}
