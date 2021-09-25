package main.jobs.fails;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("main.jobs.fails.JobTaskletFailConfiguration")
@Configuration
public class JobTaskletFailConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory steps;

    @Bean
    public Job jobFail() {
        return jobBuilderFactory.get("main.jobs.fails.JobTaskletFailConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(stepFail())
                .build();
    }

    Step stepFail() {
        return steps
                .get("stepFail")
                .tasklet((contribution, chunkContext) -> {

                    contribution.incrementReadCount();

                    contribution.incrementReadSkipCount();
                    contribution.incrementReadSkipCount();

                    contribution.incrementWriteCount(3);
                    contribution.incrementWriteSkipCount();
                    contribution.incrementWriteSkipCount();
                    contribution.incrementWriteSkipCount();
                    contribution.incrementWriteSkipCount();

                    contribution.incrementFilterCount(5);

                    contribution.incrementProcessSkipCount();
                    contribution.incrementProcessSkipCount();
                    contribution.incrementProcessSkipCount();
                    contribution.incrementProcessSkipCount();
                    contribution.incrementProcessSkipCount();
                    contribution.incrementProcessSkipCount();

                    throw new RuntimeException("step that must fail");
                })
                .build();
    }


}
