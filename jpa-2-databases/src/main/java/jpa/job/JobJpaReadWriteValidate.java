package jpa.job;

import jakarta.persistence.EntityManagerFactory;
import jpa.configuration.h2.entity.InputEntity;
import jpa.configuration.h2.entity.InputStatus;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Profile("main.jobs.jdbc.performance.JobJpaReadWriteValidate")
@Configuration
@RequiredArgsConstructor
class JobJpaReadWriteValidate {

    static final String JOB_NAME = JobJpaReadWriteValidate.class.getName();

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;
    private final PlatformTransactionManager transactionManager;

    @Qualifier("h2EMFB")
    private final EntityManagerFactory h2EMFB;

    // used for checks
    private final List<InputEntity> inputEntities = new ArrayList<>();

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
                    inputEntities.addAll(InputGenerator.generate(count));

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
                    if (actualCount != count) {
                        throw new RuntimeException("expected " + count + " found " + actualCount);
                    }

                    // check data
                    List<OutputEntity> outputEntities = outputEntityRepository.findAll();
                    Map<Integer, InputEntity> map = inputEntities.stream().collect(Collectors.toMap(InputEntity::getId, Function.identity()));
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
                inputEntityRepository.save(item.getInput());
                outputEntityRepository.save(item.getOutput());
            }
        };

        return new StepBuilder("processingStep", jobRepository)

                // larger is faster but requires more memory
                .<InputEntity, ProcessResult>chunk(1000, transactionManager)

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
                .writer(writer)

                // executor for parallel running
                .taskExecutor(new SimpleAsyncTaskExecutor("performanceTaskExecutor"))

                //job configuration done
                .build();
    }

}
