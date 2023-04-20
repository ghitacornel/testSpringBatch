package jpa.job;

import jpa.configuration.h2.entity.InputEntity;
import jpa.configuration.h2.entity.InputStatus;
import jpa.configuration.h2.repository.InputEntityRepository;
import jpa.configuration.hsql.entity.OutputEntity;
import jpa.configuration.hsql.repository.OutputEntityRepository;
import jpa.exception.SpecificException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
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
//                    inputEntities.get(100).setId(-100);// this will fail validation

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
                    count = count - 1;// exactly 2 fails validation
                    if (actualCount != count) {
                        throw new RuntimeException("expected " + count + " found " + actualCount);
                    }

                    // item that fails processing is not saved
                    outputEntityRepository.findById(1000).ifPresent(outputEntity -> {
                        throw new RuntimeException("id 1000 still present");
                    });

//                    // item with negative id is not persisted
//                    outputEntityRepository.findById(-100).ifPresent(outputEntity -> {
//                        throw new RuntimeException("id -100 still present");
//                    });
//                    outputEntityRepository.findById(100).ifPresent(outputEntity -> {
//                        throw new RuntimeException("id 100 still present");
//                    });

                    // check input data status
                    inputEntityRepository.findAll().forEach(inputEntity -> {
                        if (inputEntity.getId().equals(1000)) {
                            if (!InputStatus.NEW.equals(inputEntity.getStatus())) {
                                throw new RuntimeException("status not NEW for " + inputEntity);
                            }
                        } else if (!InputStatus.PROCESSED.equals(inputEntity.getStatus())) {
                            throw new RuntimeException("status not PROCESSED for " + inputEntity);
                        }
                    });

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private Step processingStep() {

        JpaPagingItemReader<InputEntity> reader = new JpaPagingItemReader<>();
        reader.setQueryString("select t from InputEntity t");
        reader.setEntityManagerFactory(h2EMFB);
        reader.setPageSize(1000);

        ItemWriter<ProcessResult> writer = items -> {
            for (ProcessResult item : items) {
                // really BAD idea to write back in the INPUT data source
                inputEntityRepository.save(item.getInput());
                outputEntityRepository.save(item.getOutput());
            }
        };

        return stepBuilderFactory.get("processingStep")

                // distributed transaction management since we are using 2 different databases
                .transactionManager(chainTxManager)

                // larger is faster but requires more memory
                .<InputEntity, ProcessResult>chunk(1000)

                // skip on exception writing
                .faultTolerant()
                .skipPolicy((t, skipCount) -> {
                    if (t instanceof ConstraintViolationException) return true;
                    if (t instanceof SpecificException) return true;
                    return false;
                })

                // reader/EXTRACT
                .reader(reader)

                // processor/TRANSFORM
                .processor((ItemProcessor<InputEntity, ProcessResult>) input -> {

                    // make sure exactly 1 item fails processing
                    if (input.getId().equals(1000)) {
                        throw new SpecificException();
                    }

                    input.setStatus(InputStatus.PROCESSED);

                    OutputEntity output = new OutputEntity();
                    output.setId(input.getId());
                    output.setFirstName(input.getFirstName());
                    output.setLastName(input.getLastName());
                    output.setAge(input.getAge() + 1);
                    output.setSalary(input.getSalary() + 2);
                    output.setDifference(output.getSalary() - output.getAge());

                    return ProcessResult.builder()
                            .input(input)
                            .output(output)
                            .build();
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
