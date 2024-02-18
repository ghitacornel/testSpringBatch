package tasklet.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job job(JobTaskletExecutionListener job1ExecutionListener, JobTaskletStepExecutionListener jobTaskletStepExecutionListener) {
        return new JobBuilder("main.jobs.tasklet.JobConfiguration", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1(jobTaskletStepExecutionListener))
                .next(step2())
                .listener(job1ExecutionListener)
                .build();
    }

    Step step1(JobTaskletStepExecutionListener jobTaskletStepExecutionListener) {
        return new StepBuilder("singleExecutionStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info("single execution step");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .listener(jobTaskletStepExecutionListener)
                .build();
    }

    Step step2() {
        return new StepBuilder("repeatableExecutionStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    Integer attribute = (Integer) chunkContext.getAttribute("counts");
                    if (attribute == null) attribute = 0;
                    if (attribute < 3) {
                        attribute++;
                        chunkContext.setAttribute("counts", attribute);
                        log.info("repeatableExecutionStep executed " + attribute);
                        return RepeatStatus.CONTINUABLE;
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

}
