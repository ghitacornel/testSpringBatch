package jpa.job;

import jakarta.persistence.EntityManagerFactory;
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
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
class JobJpaReadWritePerformanceMultipleThreadsConfiguration {

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;

    @Qualifier("transactionManager")
    private final PlatformTransactionManager transactionManager;

    @Qualifier("inputEntityManager")
    private final EntityManagerFactory inputEntityManager;

    @Qualifier("outputEntityManager")
    private final EntityManagerFactory outputEntityManager;

    @Bean
    Job jobJpaReadWritePerformanceMultipleThreads() {

        return new JobBuilder("jobJpaReadWritePerformanceMultipleThreads", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(new StepBuilder("clean databases", jobRepository)
                        .tasklet((contribution, chunkContext) -> {
                            inputEntityRepository.deleteAll();
                            outputEntityRepository.deleteAll();
                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .build())
                .next(new StepBuilder("generate dummy data", jobRepository)
                        .tasklet((contribution, chunkContext) -> {

                            // generate data
                            long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                            List<InputEntity> list = InputGenerator.generate(count);

                            // write generated data
                            inputEntityRepository.saveAll(list);

                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .build())
                .next(new StepBuilder("processingStep", jobRepository)
                        .<InputEntity, OutputEntity>chunk(1000, transactionManager)
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
