package tasklet.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
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
    public Job job(CustomJobExecutionListener jobExecutionListener, CustomStepExecutionListener stepExecutionListener) {
        return new JobBuilder("main.jobs.tasklet.JobConfiguration", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(new StepBuilder("singleExecutionStep", jobRepository)
                        .tasklet((contribution, chunkContext) -> {
                            log.info("single execution step");
                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .listener(stepExecutionListener)
                        .build())
                .next(new StepBuilder("repeatableExecutionStep", jobRepository)
                        .tasklet((contribution1, chunkContext1) -> {
                            Integer attribute = (Integer) chunkContext1.getAttribute("counts");
                            if (attribute == null) {
                                attribute = 0;
                            }
                            if (attribute < 3) {
                                attribute++;
                                chunkContext1.setAttribute("counts", attribute);
                                log.info("repeatableExecutionStep executed " + attribute);
                                return RepeatStatus.CONTINUABLE;
                            }
                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .build())
                .listener(jobExecutionListener)
                .build();
    }

}
