package tasklet.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.parameters.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
class JobConfiguration {

    private final JobRepository jobRepository;

    @Bean
    Job job(CustomJobExecutionListener jobExecutionListener, CustomStepExecutionListener stepExecutionListener) {
        return new JobBuilder("main.jobs.tasklet.JobConfiguration", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(new StepBuilder("singleExecutionStep", jobRepository)
                        .tasklet((_, _) -> {
                            log.info("single execution step");
                            return RepeatStatus.FINISHED;
                        })
                        .listener(stepExecutionListener)
                        .build())
                .next(new StepBuilder("repeatableExecutionStep", jobRepository)
                        .tasklet((_, chunkContext1) -> {
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
                        })
                        .build())
                .listener(jobExecutionListener)
                .build();
    }

}
