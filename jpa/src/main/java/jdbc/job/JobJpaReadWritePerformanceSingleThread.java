package jdbc.job;

import jdbc.configuration.h2.entity.InputEntity;
import jdbc.configuration.h2.repository.InputEntityRepository;
import jdbc.configuration.hsql.entity.OutputEntity;
import jdbc.configuration.hsql.repository.OutputEntityRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Profile("main.jobs.jdbc.performance.JobJdbcReadWritePerformanceSingleThread")
@Configuration
public class JobJpaReadWritePerformanceSingleThread {

    static final String JOB_NAME = JobJpaReadWritePerformanceSingleThread.class.getName();

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private InputEntityRepository inputEntityRepository;
    @Autowired
    private OutputEntityRepository outputEntityRepository;

    @Autowired
    EntityManagerFactory h2EMFB;
    @Autowired
    EntityManagerFactory hsqlEMFB;

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
                    List<InputEntity> list = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        InputDTO inputDTO = InputDTO.generate();
                        InputEntity inputEntity = new InputEntity();
                        inputEntity.setId(inputDTO.getId());
                        inputEntity.setFirstName(inputDTO.getFirstName());
                        inputEntity.setLastName(inputDTO.getLastName());
                        inputEntity.setAge(inputDTO.getAge());
                        inputEntity.setSalary(inputDTO.getSalary());
                        list.add(inputEntity);
                    }

                    // write generated data
                    inputEntityRepository.saveAll(list);

                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private Step verifyDatabaseStep() {// only a count is performed as validation
        return stepBuilderFactory
                .get("verifyDatabaseStep")
                .tasklet((contribution, chunkContext) -> {
                    long actualCount = outputEntityRepository.count();
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    if (actualCount != count) {
                        throw new RuntimeException("expected " + count + " found " + actualCount);
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private Step processingStep() {

        JpaCursorItemReader<InputEntity> jpaCursorItemReader = new JpaCursorItemReader<>();
        jpaCursorItemReader.setQueryString("select t from InputEntity t");
        jpaCursorItemReader.setEntityManagerFactory(h2EMFB);
        try {
            jpaCursorItemReader.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JpaItemWriter<OutputEntity> jpaItemWriter = new JpaItemWriter<>();
        jpaItemWriter.setEntityManagerFactory(hsqlEMFB);
        jpaItemWriter.setUsePersist(true);
        try {
            jpaItemWriter.afterPropertiesSet();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return stepBuilderFactory.get("processingStep")

                // larger is faster but requires more memory
                .<InputEntity, OutputEntity>chunk(1000)

                // reader/EXTRACT
                .reader(jpaCursorItemReader)

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
                .writer(jpaItemWriter)

                //job configuration done
                .build();
    }

}
