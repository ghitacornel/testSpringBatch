package jdbc.job;

import jdbc.configuration.h2.entity.InputEntity;
import jdbc.configuration.h2.repository.InputEntityRepository;
import jdbc.configuration.hsql.repository.OutputEntityRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    @Qualifier("dataSourceH2")
    private DataSource dataSourceH2;

    @Autowired
    @Qualifier("dataSourceHSQL")
    private DataSource dataSourceHSQL;

    @Autowired
    private InputEntityRepository inputEntityRepository;
    @Autowired
    private OutputEntityRepository outputEntityRepository;

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
                    Connection connection = dataSourceHSQL.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement("select count(*) from OutputDTO");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    long actualCount = resultSet.getLong(1);
                    preparedStatement.close();
                    connection.close();
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    if (actualCount != count) {
                        throw new RuntimeException("expected " + count + " found " + actualCount);
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    private Step processingStep() {
        return stepBuilderFactory.get("processingStep")

                // larger is faster but requires more memory
                .<InputDTO, OutputDTO>chunk(1000)

                // reader/EXTRACT
                .reader(new JdbcCursorItemReaderBuilder<InputDTO>()
                        .name("jdbcCursorItemReader")
                        .dataSource(dataSourceH2)
                        .sql("select * from InputDTO")// programmer is responsible for filtering the data to be processed
                        .rowMapper(new BeanPropertyRowMapper<>(InputDTO.class))
                        .build())

                // processor/TRANSFORM
                .processor((ItemProcessor<InputDTO, OutputDTO>) input -> {
                    OutputDTO output = new OutputDTO();
                    output.setId(input.getId());
                    output.setFirstName(input.getFirstName());
                    output.setLastName(input.getLastName());
                    output.setAge(input.getAge() + 1);
                    output.setSalary(input.getSalary() + 2);
                    output.setDifference(output.getSalary() - output.getAge());
                    return output;
                })

                // writer/LOAD
                .writer(new JdbcBatchItemWriterBuilder<OutputDTO>()
                        .dataSource(dataSourceHSQL)
                        .sql("INSERT INTO OutputDTO(id,firstName,lastName,salary,age,difference) VALUES (?,?,?,?,?,?)")
                        .itemPreparedStatementSetter((item, ps) -> {
                            ps.setInt(1, item.getId());
                            ps.setString(2, item.getFirstName());
                            ps.setString(3, item.getLastName());
                            ps.setLong(4, item.getSalary());
                            ps.setInt(5, item.getAge());
                            ps.setLong(6, item.getDifference());
                        })
                        .build())

                //job configuration done
                .build();
    }

}
