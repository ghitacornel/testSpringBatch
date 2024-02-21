package decider.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Objects;

@Configuration
@RequiredArgsConstructor
class JobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    Job job(JobExecutionDecider jobExecutionDecider) {
        return new JobBuilder("main.jobs.decider.JobConfiguration", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .next(jobExecutionDecider).on("2").to(step2())
                .from(jobExecutionDecider).on("3").to(step3()).next(step31())
                .from(jobExecutionDecider).on("4").to(step4()).next(step41()).next(step42())
                .end()
                .build();
    }

    private Step step1() {
        return new StepBuilder("step1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step1", "step1");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    // mandatory a @Bean since it is used multiple times as a root starting point
    @Bean
    JobExecutionDecider jobExecutionDecider() {
        return (jobExecution, stepExecution) -> new FlowExecutionStatus(Objects.requireNonNull(jobExecution.getJobParameters().getString("path")));
    }

    private Step step2() {
        return new StepBuilder("step2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step2", "step2");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step step3() {
        return new StepBuilder("step3", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step3", "step3");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step step31() {
        return new StepBuilder("step31", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step31", "step31");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step step4() {
        return new StepBuilder("step4", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step4", "step4");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step step41() {
        return new StepBuilder("step41", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step41", "step41");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step step42() {
        return new StepBuilder("step42", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step42", "step42");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

}
