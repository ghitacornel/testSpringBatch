package jpa.job;

import jakarta.persistence.EntityManagerFactory;
import jpa.configuration.input.entity.InputEntity;
import jpa.configuration.input.entity.InputStatus;
import jpa.configuration.input.repository.InputEntityRepository;
import jpa.configuration.output.entity.OutputEntity;
import jpa.configuration.output.repository.OutputEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JpaPagingItemReader;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
class JobJpaReadWriteValidateConfiguration {

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;

    @Qualifier("inputEntityManager")
    private final EntityManagerFactory inputEntityManager;

    // used for checks
    private final List<InputEntity> inputEntities = new ArrayList<>();

    @Bean
    Job jobJpaReadWriteValidate() {

        JpaPagingItemReader<InputEntity> reader = new JpaPagingItemReader<>(inputEntityManager);
        reader.setQueryString("select t from InputEntity t");
        reader.setPageSize(1000);

        return new JobBuilder("jobJpaReadWriteValidate", jobRepository)
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

                            // write generated data
                            inputEntityRepository.saveAll(inputEntities);

                            return RepeatStatus.FINISHED;
                        })
                        .build())
                .next(new StepBuilder("processingStep", jobRepository)

                        // larger is faster but requires more memory
                        .<InputEntity, ProcessResult>chunk(1000)

                        // reader/EXTRACT
                        .reader(reader)

                        // processor/TRANSFORM
                        .processor(input -> {

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
                        .writer(items -> {
                            for (ProcessResult item : items) {
                                // really BAD idea to write back in the INPUT data source
                                inputEntityRepository.save(item.getInput());
                                outputEntityRepository.save(item.getOutput());
                            }
                        })
                        .taskExecutor(new SimpleAsyncTaskExecutor("performanceTaskExecutor"))
                        .build())
                .next(new StepBuilder("verifyDatabaseStep", jobRepository)
                        .tasklet((_, chunkContext1) -> {

                            // check count
                            long actualCount = outputEntityRepository.count();
                            long count = (long) chunkContext1.getStepContext().getJobParameters().get("count");
                            if (actualCount != count) {
                                throw new RuntimeException("expected " + count + " found " + actualCount);
                            }

                            // check data
                            List<OutputEntity> outputEntities = outputEntityRepository.findAll();
                            Map<Integer, InputEntity> map = inputEntities.stream()
                                    .collect(Collectors.toMap(InputEntity::getId, Function.identity()));
                            outputEntities.forEach(outputEntity -> {
                                InputEntity inputEntity = map.get(outputEntity.getId());
                                if (inputEntity == null) {
                                    throw new RuntimeException("missing input id" + outputEntity.getId());
                                }
                                if (!outputEntity.getFirstName().equals(inputEntity.getFirstName()) ||
                                        !outputEntity.getLastName().equals(inputEntity.getLastName()) ||
                                        outputEntity.getAge() != inputEntity.getAge() + 1 ||
                                        outputEntity.getSalary() != inputEntity.getSalary() + 2 ||
                                        outputEntity.getDifference() != outputEntity.getSalary() - outputEntity.getAge()) {
                                    throw new RuntimeException("mismatch " + outputEntity + " with " + inputEntity);
                                }
                            });

                            // check input data status
                            inputEntityRepository.findAll().forEach(inputEntity -> {
                                if (!InputStatus.PROCESSED.equals(inputEntity.getStatus())) {
                                    throw new RuntimeException("status not processed for " + inputEntity);
                                }
                            });

                            return RepeatStatus.FINISHED;
                        })
                        .build())
                .build();
    }

}
