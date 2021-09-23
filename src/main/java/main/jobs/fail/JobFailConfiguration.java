package main.jobs.fail;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JobFailConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory steps;

    @Bean
    public Job jobFail() {
        return jobBuilderFactory.get("jobFail")
                .incrementer(new RunIdIncrementer())
                .start(stepFail())
                .build();
    }

    Step stepFail() {
        return steps
                .get("stepFail")
                .tasklet((contribution, chunkContext) -> {
                    throw new RuntimeException("step that must fail");
                })
                .build();
    }


}
