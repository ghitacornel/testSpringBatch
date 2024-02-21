package jobscope.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job job(@Qualifier("step1") Step step1,@Qualifier("step2") Step step2, JobListener jobListener) {
        return new JobBuilder("main.jobs.jobscope.JobConfiguration", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .next(step2)
                .listener(jobListener)
                .build();
    }

    @Bean
    Step step1(Tasklet1 tasklet, Step1Listener listener) {
        return new StepBuilder("step1", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(listener)
                .build();
    }

    @Bean
    Step step2(Tasklet2 tasklet, Step2Listener listener) {
        return new StepBuilder("step2", jobRepository)
                .tasklet(tasklet, transactionManager)
                .listener(listener)
                .build();
    }

}
