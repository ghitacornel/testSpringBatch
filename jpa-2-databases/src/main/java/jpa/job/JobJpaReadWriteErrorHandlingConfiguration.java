package jpa.job;

import jakarta.persistence.EntityManagerFactory;
import jakarta.validation.ConstraintViolationException;
import jpa.configuration.input.entity.InputEntity;
import jpa.configuration.input.entity.InputStatus;
import jpa.configuration.input.repository.InputEntityRepository;
import jpa.configuration.output.entity.OutputEntity;
import jpa.configuration.output.repository.OutputEntityRepository;
import jpa.exception.SpecificException;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
class JobJpaReadWriteErrorHandlingConfiguration {

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;

    @Qualifier("inputEntityManager")
    private final EntityManagerFactory inputEntityManager;

    // used for checks
    private final List<InputEntity> inputEntities = new ArrayList<>();

    @Bean
    Job jobJpaReadWriteErrorHandling() {

        JpaPagingItemReader<InputEntity> reader = new JpaPagingItemReader<>(inputEntityManager);
        reader.setQueryString("select t from InputEntity t");
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

        return new JobBuilder("jobJpaReadWriteErrorHandling", jobRepository)
                .start(new StepBuilder("clean databases", jobRepository)
                        .tasklet((_, _) -> {
                            inputEntityRepository.deleteAll();
                            outputEntityRepository.deleteAll();
                            return RepeatStatus.FINISHED;
                        })
                        .build())
                .next(new StepBuilder("generate dummy data", jobRepository)
                        .tasklet((_, chunkContext) -> {

                            // generate data
                            long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                            inputEntities.addAll(InputGenerator.generate(count));
                            inputEntities.get(100).setId(-100);// this will fail validation

                            // write generated data
                            inputEntityRepository.saveAll(inputEntities);

                            return RepeatStatus.FINISHED;
                        })
                        .build())
                .next(new StepBuilder("processingStep", jobRepository)
                        .<InputEntity, ProcessResult>chunk(1000)
                        .faultTolerant()
                        .skipPolicy((t, _) -> {
                            if (t instanceof ConstraintViolationException) return true;
                            if (t instanceof SpecificException) return true;
                            return false;
                        })
                        .reader(reader)
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
                        .writer(writer)
                        .taskExecutor(new SimpleAsyncTaskExecutor("performanceTaskExecutor"))
                        .build())
                .next(new StepBuilder("verifyDatabaseStep", jobRepository)
                        .tasklet((_, chunkContext1) -> {

                            // check count
                            long actualCount = outputEntityRepository.count();
                            long count = (long) chunkContext1.getStepContext().getJobParameters().get("count");
                            count = count - 2;// exactly 2 fails validation
                            if (actualCount != count) {
                                throw new RuntimeException("expected " + count + " found " + actualCount);
                            }

                            // item that fails processing is not saved
                            outputEntityRepository.findById(1000).ifPresent(outputEntity -> {
                                throw new RuntimeException("id 1000 still present");
                            });

                            // item with negative id is not persisted
                            outputEntityRepository.findById(-100).ifPresent(_ -> {
                                throw new RuntimeException("id -100 still present");
                            });
                            outputEntityRepository.findById(100).ifPresent(_ -> {
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
                        })
                        .build())
                .build();
    }

}
