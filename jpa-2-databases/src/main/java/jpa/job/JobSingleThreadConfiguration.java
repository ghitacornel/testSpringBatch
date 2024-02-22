package jpa.job;

import jpa.configuration.input.entity.InputEntity;
import jpa.configuration.input.repository.InputEntityRepository;
import jpa.configuration.output.entity.OutputEntity;
import jpa.configuration.output.repository.OutputEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Collections;
import java.util.List;

@Configuration
@RequiredArgsConstructor
class JobSingleThreadConfiguration {

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    Job jobSingleThread() {

        return new JobBuilder("jobSingleThread", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(new StepBuilder("createDummyData", jobRepository)
                        .tasklet((contribution, chunkContext) -> {

                            //cleanup databases
                            inputEntityRepository.deleteAll();
                            outputEntityRepository.deleteAll();

                            // generate data
                            long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                            List<InputEntity> list = InputGenerator.generate(count);

                            // write generated data
                            inputEntityRepository.saveAll(list);

                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .build())
                .next(new StepBuilder("migrateData", jobRepository)

                        // larger is faster but requires more memory
                        .<InputEntity, OutputEntity>chunk(1000, transactionManager)
                        .reader(new RepositoryItemReaderBuilder<InputEntity>()
                                .name("inputEntityReader")
                                .repository(inputEntityRepository)
                                .sorts(Collections.singletonMap("id", Sort.Direction.ASC))
                                .methodName("findAll")
                                .pageSize(100)
                                .build())
                        .processor(input -> OutputEntity.builder()
                                .id(input.getId())
                                .firstName(input.getFirstName())
                                .lastName(input.getLastName())
                                .age(input.getAge() + 1)
                                .salary(input.getSalary() + 2)
                                .difference(input.getSalary() - input.getAge())
                                .build())
                        .writer(new RepositoryItemWriterBuilder<OutputEntity>()
                                .repository(outputEntityRepository)
                                .build())
                        .build())
                .next(new StepBuilder("verifyMigratedData", jobRepository)
                        .tasklet((contribution1, chunkContext1) -> {
                            long actualCount = outputEntityRepository.count();
                            long count1 = (long) chunkContext1.getStepContext().getJobParameters().get("count");
                            if (actualCount != count1) {
                                throw new RuntimeException("expected " + count1 + " found " + actualCount);
                            }
                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .build())
                .build();
    }

}
