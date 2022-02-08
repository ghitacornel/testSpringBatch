package decider.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory steps;

    @Bean
    public Job job(JobExecutionDecider jobExecutionDecider) {
        return jobBuilderFactory.get("main.jobs.decider.JobConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(step1())
                .next(jobExecutionDecider).on("2").to(step2())
                .from(jobExecutionDecider).on("3").to(step3()).next(step31())
                .from(jobExecutionDecider).on("4").to(step4()).next(step41()).next(step42())
                .end()
                .build();
    }

    Step step1() {
        return steps
                .get("step1")
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step1", "step1");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    // mandatory a @Bean since it is used multiple times as a root starting point
    @Bean
    JobExecutionDecider jobExecutionDecider() {
        return (jobExecution, stepExecution) -> new FlowExecutionStatus(Objects.requireNonNull(jobExecution.getJobParameters().getString("path")));
    }


    Step step2() {
        return steps
                .get("step2")
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step2", "step2");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    Step step3() {
        return steps
                .get("step3")
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step3", "step3");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    Step step31() {
        return steps
                .get("step31")
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step31", "step31");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    Step step4() {
        return steps
                .get("step4")
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step4", "step4");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    Step step41() {
        return steps
                .get("step41")
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step41", "step41");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    Step step42() {
        return steps
                .get("step42")
                .tasklet((contribution, chunkContext) -> {
                    chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext().putString("step42", "step42");
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
