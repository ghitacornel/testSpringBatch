package main.jobs.tasklets;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("main.jobs.tasklets.JobTaskletConfiguration")
@Configuration
public class JobTaskletConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory steps;

    @Bean
    public Job job1(JobTaskletExecutionListener job1ExecutionListener, JobTaskletStepExecutionListener jobTaskletStepExecutionListener) {
        return jobBuilderFactory.get("main.jobs.tasklets.JobTaskletConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(step1(jobTaskletStepExecutionListener))
                .next(step2())
                .listener(job1ExecutionListener)
                .build();
    }

    Step step1(JobTaskletStepExecutionListener jobTaskletStepExecutionListener) {
        return steps
                .get("singleExecutionStep")
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("single execution step");
                    return RepeatStatus.FINISHED;
                })
                .listener(jobTaskletStepExecutionListener)
                .build();
    }

    Step step2() {
        return steps
                .get("repeatableExecutionStep")
                .tasklet((contribution, chunkContext) -> {
                    Integer attribute = (Integer) chunkContext.getAttribute("counts");
                    if (attribute == null) attribute = 0;
                    if (attribute < 3) {
                        attribute++;
                        chunkContext.setAttribute("counts", attribute);
                        System.out.println("repeatableExecutionStep executed " + attribute);
                        return RepeatStatus.CONTINUABLE;
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

}
