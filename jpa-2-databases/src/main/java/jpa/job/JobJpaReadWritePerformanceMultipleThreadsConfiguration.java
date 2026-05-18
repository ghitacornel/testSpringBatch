package jpa.job;

import jakarta.persistence.EntityManagerFactory;
import jpa.configuration.input.entity.InputEntity;
import jpa.configuration.input.repository.InputEntityRepository;
import jpa.configuration.output.entity.OutputEntity;
import jpa.configuration.output.repository.OutputEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.List;

@Configuration
@RequiredArgsConstructor
class JobJpaReadWritePerformanceMultipleThreadsConfiguration {

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;

    @Qualifier("inputEntityManager")
    private final EntityManagerFactory inputEntityManager;

    @Bean
    Job jobJpaReadWritePerformanceMultipleThreads() {

        return new JobBuilder("jobJpaReadWritePerformanceMultipleThreads", jobRepository)
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
                            List<InputEntity> list = InputGenerator.generate(count);

                            // write generated data
                            inputEntityRepository.saveAll(list);

                            return RepeatStatus.FINISHED;
                        })
                        .build())
                .next(new StepBuilder("processingStep", jobRepository)
                        .<InputEntity, OutputEntity>chunk(1000)
                        .reader(new JpaPagingItemReaderBuilder<InputEntity>()
                                .queryString("select t from InputEntity t order by id")
                                .entityManagerFactory(inputEntityManager)
                                .pageSize(1000)
                                .saveState(false)
                                .build())
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
                        .writer(new RepositoryItemWriterBuilder<OutputEntity>()
                                .repository(outputEntityRepository)
                                .build())
                        .taskExecutor(new SimpleAsyncTaskExecutor("performanceTaskExecutor"))
                        .build())
                .next(new StepBuilder("verifyDatabaseStep", jobRepository)
                        .tasklet((_, chunkContext1) -> {
                            long actualCount = outputEntityRepository.count();
                            long count1 = (long) chunkContext1.getStepContext().getJobParameters().get("count");
                            if (actualCount != count1) {
                                throw new RuntimeException("expected " + count1 + " found " + actualCount);
                            }
                            return RepeatStatus.FINISHED;
                        })
                        .build())
                .build();
    }

}
