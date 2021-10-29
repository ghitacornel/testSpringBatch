package main.jobs.jobscope;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("main.jobs.jobscope.JobScopeConfiguration")
@Configuration
public class JobScopeConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory steps;

    @Bean
    public Job job(Step step1, Step step2) {
        return jobBuilderFactory.get("main.jobs.jobscope.JobScopeConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .build();
    }

    @Bean
    Step step1(Tasklet1 tasklet, Step1Listener listener) {
        return steps
                .get("step1")
                .tasklet(tasklet)
                .listener(listener)
                .build();
    }

    @Bean
    Step step2(Tasklet2 tasklet, Step2Listener listener) {
        return steps
                .get("step2")
                .tasklet(tasklet)
                .listener(listener)
                .build();
    }

}
