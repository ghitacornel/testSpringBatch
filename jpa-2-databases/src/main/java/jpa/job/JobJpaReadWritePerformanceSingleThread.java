package jpa.job;

import jpa.configuration.h2.entity.InputEntity;
import jpa.configuration.h2.repository.InputEntityRepository;
import jpa.configuration.hsql.entity.OutputEntity;
import jpa.configuration.hsql.repository.OutputEntityRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Profile("main.jobs.jdbc.performance.JobJdbcReadWritePerformanceSingleThread")
@Configuration
public class JobJpaReadWritePerformanceSingleThread {

    static final String JOB_NAME = JobJpaReadWritePerformanceSingleThread.class.getName();

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private InputEntityRepository inputEntityRepository;
    @Autowired
    private OutputEntityRepository outputEntityRepository;

    @Autowired
    private EntityManagerFactory h2EMFB;
    @Autowired
    private EntityManagerFactory hsqlEMFB;
    @Autowired
    private PlatformTransactionManager chainTxManager;

    @Bean
    public Job job() {
        return jobBuilderFactory.get(JOB_NAME)
                .incrementer(new RunIdIncrementer())
                .start(createDataStep())
                .next(processingStep())
                .next(verifyDatabaseStep())
                .build();
    }

    private Step createDataStep() {
        return stepBuilderFactory
                .get("createDataStep")
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
                })
                .build();
    }

    private Step verifyDatabaseStep() {// only a count is performed as validation
        return stepBuilderFactory
                .get("verifyDatabaseStep")
                .tasklet((contribution, chunkContext) -> {
                    long actualCount = outputEntityRepository.count();
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    if (actualCount != count) {
                        throw new RuntimeException("expected " + count + " found " + actualCount);
                    }
                    return RepeatStatus.FINISHED;
                })
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

        return stepBuilderFactory.get("processingStep")

                // distributed transaction management since we are using 2 different databases
                .transactionManager(chainTxManager)

                // larger is faster but requires more memory
                .<InputEntity, OutputEntity>chunk(1000)

                // reader/EXTRACT
                .reader(reader)

                // processor/TRANSFORM
                .processor((ItemProcessor<InputEntity, OutputEntity>) input -> {
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