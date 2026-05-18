package tasklet.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
class JobConfiguration {

    private final JobRepository jobRepository;

    @Bean
    Job job() {
        return new JobBuilder("main.jobs.tasklet.fails.JobConfiguration", jobRepository)
                .start(step())
                .build();
    }

    private Step step() {
        return new StepBuilder("stepFail", jobRepository)
                .tasklet((contribution, _) -> {

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
