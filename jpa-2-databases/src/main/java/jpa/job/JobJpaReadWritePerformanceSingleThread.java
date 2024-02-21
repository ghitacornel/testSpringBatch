package jpa.job;

import jakarta.persistence.EntityManagerFactory;
import jpa.configuration.h2.entity.InputEntity;
import jpa.configuration.h2.repository.InputEntityRepository;
import jpa.configuration.hsql.entity.OutputEntity;
import jpa.configuration.hsql.repository.OutputEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Profile("main.jobs.jdbc.performance.JobJdbcReadWritePerformanceSingleThread")
@Configuration
@RequiredArgsConstructor
class JobJpaReadWritePerformanceSingleThread {

    static final String JOB_NAME = JobJpaReadWritePerformanceSingleThread.class.getName();

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;
    private final EntityManagerFactory h2EMFB;
    private final EntityManagerFactory hsqlEMFB;
    private final PlatformTransactionManager transactionManager;

    @Bean
    Job job() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(createDataStep())
                .next(processingStep())
                .next(verifyDatabaseStep())
                .build();
    }

    private Step createDataStep() {
        return new StepBuilder("createDataStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    //cleanup INPUT database
                    inputEntityRepository.deleteAll();

                    //cleanup OUTPUT database
                    outputEntityRepository.deleteAll();

                    // generate data
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    List<InputEntity> list = InputGenerator.generate(count);

                    // write generated data
                    inputEntityRepository.saveAll(list);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step verifyDatabaseStep() {// only a count is performed as validation
        return new StepBuilder("verifyDatabaseStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    long actualCount = outputEntityRepository.count();
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    if (actualCount != count) {
                        throw new RuntimeException("expected " + count + " found " + actualCount);
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step processingStep() {

        JpaPagingItemReader<InputEntity> reader = new JpaPagingItemReader<>();
        reader.setQueryString("select t from InputEntity t");
        reader.setEntityManagerFactory(h2EMFB);
        reader.setPageSize(1000);
        reader.setSaveState(false);

        JpaItemWriter<OutputEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(hsqlEMFB);
        writer.setUsePersist(true);

        return new StepBuilder("processingStep", jobRepository)

                // larger is faster but requires more memory
                .<InputEntity, OutputEntity>chunk(1000, transactionManager)

                // reader/EXTRACT
                .reader(reader)

                // processor/TRANSFORM
                .processor(input -> {
                    OutputEntity output = new OutputEntity();
                    output.setId(input.getId());
                    output.setFirstName(input.getFirstName());
                    output.setLastName(input.getLastName());
                    output.setAge(input.getAge() + 1);
                    output.setSalary(input.getSalary() + 2);
                    output.setDifference(output.getSalary() - output.getAge());
                    return output;
                })

                // writer/LOAD
                .writer(writer)

                //job configuration done
                .build();
    }

}
