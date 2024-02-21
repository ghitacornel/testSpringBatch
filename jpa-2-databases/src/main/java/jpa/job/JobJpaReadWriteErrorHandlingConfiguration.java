package jpa.job;

import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.ConstraintViolationException;
import jpa.configuration.h2.entity.InputEntity;
import jpa.configuration.h2.entity.InputStatus;
import jpa.configuration.h2.repository.InputEntityRepository;
import jpa.configuration.hsql.entity.OutputEntity;
import jpa.configuration.hsql.repository.OutputEntityRepository;
import jpa.exception.SpecificException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
class JobJpaReadWriteErrorHandlingConfiguration {

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;
    private final PlatformTransactionManager transactionManager;

    @Qualifier("h2EMFB")
    private final EntityManagerFactory h2EMFB;

    // used for checks
    private final List<InputEntity> inputEntities = new ArrayList<>();

    @Bean
    Job jobJpaReadWriteErrorHandling() {
        return new JobBuilder("jobJpaReadWriteErrorHandling", jobRepository)
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
                    inputEntities.addAll(InputGenerator.generate(count));
                    inputEntities.get(100).setId(-100);// this will fail validation

                    // write generated data
                    inputEntityRepository.saveAll(inputEntities);

                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step verifyDatabaseStep() {
        return new StepBuilder("verifyDatabaseStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {

                    // check count
                    long actualCount = outputEntityRepository.count();
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    count = count - 2;// exactly 2 fails validation
                    if (actualCount != count) {
                        throw new RuntimeException("expected " + count + " found " + actualCount);
                    }

                    // item that fails processing is not saved
                    outputEntityRepository.findById(1000).ifPresent(outputEntity -> {
                        throw new RuntimeException("id 1000 still present");
                    });

                    // item with negative id is not persisted
                    outputEntityRepository.findById(-100).ifPresent(outputEntity -> {
                        throw new RuntimeException("id -100 still present");
                    });
                    outputEntityRepository.findById(100).ifPresent(outputEntity -> {
                        throw new RuntimeException("id 100 still present");
                    });

                    // check input data status
                    inputEntityRepository.findAll().forEach(inputEntity -> {
                        if (inputEntity.getId().equals(1000) || inputEntity.getId().equals(-100)) {
                            if (!InputStatus.NEW.equals(inputEntity.getStatus())) {
                                throw new RuntimeException("status not NEW for " + inputEntity);
                            }
                        } else if (!InputStatus.PROCESSED.equals(inputEntity.getStatus())) {
                            throw new RuntimeException("status not PROCESSED for " + inputEntity);
                        }
                    });

                    return RepeatStatus.FINISHED;
                }, transactionManager)
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
                // even worse in case of BATCH for every item
                // BETTER validate before WRITING
                inputEntityRepository.save(item.getInput());
                outputEntityRepository.save(item.getOutput());
            }
        };

        return new StepBuilder("processingStep", jobRepository)

                // larger is faster but requires more memory
                .<InputEntity, ProcessResult>chunk(1000, transactionManager)

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
                .processor(input -> {

                    // make sure exactly 1 item fails processing
                    if (input.getId().equals(1000)) {
                        throw new SpecificException();
                    }

                    if (input.getId() < 0) {
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

                //job configuration done
                .build();
    }

}
