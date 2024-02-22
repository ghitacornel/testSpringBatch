package jpa.job;

import jakarta.persistence.EntityManagerFactory;
import jpa.configuration.h2.entity.InputEntity;
import jpa.configuration.h2.repository.InputEntityRepository;
import jpa.configuration.hsql.entity.OutputEntity;
import jpa.configuration.hsql.repository.OutputEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
class JobSingleThreadConfiguration {

    private final JobRepository jobRepository;
    private final InputEntityRepository inputEntityRepository;
    private final OutputEntityRepository outputEntityRepository;
    private final PlatformTransactionManager transactionManager;

    @Qualifier("h2EMFB")
    private final EntityManagerFactory h2EMFB;

    @Qualifier("hsqlEMFB")
    private final EntityManagerFactory hsqlEMFB;

    @Bean
    Job jobSingleThread() {
        // only a count is performed as validation

        JpaPagingItemReader<InputEntity> reader = new JpaPagingItemReader<>();
        reader.setQueryString("select t from InputEntity t");
        reader.setEntityManagerFactory(h2EMFB);
        reader.setPageSize(1000);
        reader.setSaveState(false);

        JpaItemWriter<OutputEntity> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(hsqlEMFB);
        writer.setUsePersist(true);

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

                        // reader/EXTRACT
                        .reader(reader)

                        // processor/TRANSFORM
                        .processor(input -> OutputEntity.builder()
                                .id(input.getId())
                                .firstName(input.getFirstName())
                                .lastName(input.getLastName())
                                .age(input.getAge() + 1)
                                .salary(input.getSalary() + 2)
                                .difference(input.getSalary() - input.getAge())
                                .build())

                        // writer/LOAD
                        .writer(writer)

                        //job configuration done
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
