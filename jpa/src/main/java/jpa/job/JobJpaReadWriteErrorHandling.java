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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@Profile("main.jobs.jdbc.performance.JobJpaReadWriteErrorHandling")
@Configuration
public class JobJpaReadWriteErrorHandling {

    static final String JOB_NAME = JobJpaReadWriteErrorHandling.class.getName();

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

    // used for checks
    private final List<InputEntity> inputEntities = new ArrayList<>();

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
                    inputEntities.addAll(InputGenerator.generate(count));
                    inputEntities.get(100).setId(-100);// this will fail validation

                    // write generated data
                    inputEntityRepository.saveAll(inputEntities);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private Step verifyDatabaseStep() {
        return stepBuilderFactory
                .get("verifyDatabaseStep")
                .tasklet((contribution, chunkContext) -> {

                    // check count
                    long actualCount = outputEntityRepository.count();
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    count--;// exactly 1 fails validation
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

        JpaItemWriter<OutputEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(hsqlEMFB);
        writer.setUsePersist(true);

        return stepBuilderFactory.get("processingStep")

                // distributed transaction management since we are using 2 different databases
                .transactionManager(chainTxManager)

                // larger is faster but requires more memory
                .<InputEntity, OutputEntity>chunk(1000)

                .faultTolerant()
                .skipPolicy((t, skipCount) -> t instanceof ConstraintViolationException)

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

                // executor for parallel running
                .taskExecutor(new SimpleAsyncTaskExecutor("performanceTaskExecutor"))
                .throttleLimit(5)

                //job configuration done
                .build();
    }

}
