package main.batch.configuration;

import main.batch.listeners.CustomJobListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableBatchProcessing
@PropertySource("classpath:batch.properties")
public class BatchConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Bean
    public Job mainJob(CustomJobListener listener, Step step1, Step step2, Step step3, Step step4) {
        return jobBuilderFactory.get("mainJob")
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .next(step2)
                .next(step3)
                .next(step4)
                .end()
                .build();
    }

}
