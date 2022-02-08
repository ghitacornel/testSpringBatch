package tasklet.job;

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
    private final StepBuilderFactory steps;

    @Bean
    public Job job() {
        return jobBuilderFactory.get("main.jobs.tasklet.fails.JobConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(step())
                .build();
    }

    Step step() {
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