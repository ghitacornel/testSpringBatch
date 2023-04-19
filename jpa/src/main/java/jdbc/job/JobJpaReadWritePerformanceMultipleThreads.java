package jdbc.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.H2PagingQueryProvider;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Profile("main.jobs.jdbc.performance.JobJdbcReadWritePerformanceMultipleThreads")
@Configuration
public class JobJpaReadWritePerformanceMultipleThreads {

    static final String JOB_NAME = JobJpaReadWritePerformanceMultipleThreads.class.getName();

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

    @Bean
    public Job job() {
        return jobBuilderFactory.get(JobJpaReadWritePerformanceMultipleThreads.class.getName())
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
                    {
                        Connection connection = dataSourceH2.getConnection();
                        Statement statement = connection.createStatement();
                        statement.executeUpdate("truncate table InputDTO");
                        statement.close();
                    }

                    //cleanup OUTPUT database
                    {
                        Connection connection = dataSourceHSQL.getConnection();
                        Statement statement = connection.createStatement();
                        statement.executeUpdate("truncate table OutputDTO");
                        statement.close();
                    }

                    // generate data
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    List<InputDTO> list = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        InputDTO inputDTO = InputDTO.generate();
                        list.add(inputDTO);
                    }

                    // write generated data
                    Connection connection = dataSourceH2.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement("insert into InputDTO(ID,firstName,lastName,salary,age) values(?,?,?,?,?)");
                    for (InputDTO inputDTO : list) {
                        preparedStatement.setInt(1, inputDTO.getId());
                        preparedStatement.setString(2, inputDTO.getFirstName());
                        preparedStatement.setString(3, inputDTO.getLastName());
                        preparedStatement.setLong(4, inputDTO.getSalary());
                        preparedStatement.setInt(5, inputDTO.getAge());
                        preparedStatement.execute();
                    }
                    preparedStatement.close();
                    connection.close();
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

                .reader(reader())

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

                // executor for parallel running
                .taskExecutor(new SimpleAsyncTaskExecutor("performanceTaskExecutor"))
                .throttleLimit(5)

                //job configuration done
                .build();
    }

    private JdbcPagingItemReader<InputDTO> reader() {

        H2PagingQueryProvider queryProvider = new H2PagingQueryProvider();
        queryProvider.setSelectClause("*");
        queryProvider.setFromClause("InputDTO");
        queryProvider.setSortKeys(Collections.singletonMap("id", Order.ASCENDING));// need an order due to pagination
        queryProvider.setWhereClause("");// programmer is responsible for filtering the data to be processed

        JdbcPagingItemReader<InputDTO> jdbcPagingItemReader = new JdbcPagingItemReaderBuilder<InputDTO>()
                .name("jdbcPagingItemReader")
                .dataSource(dataSourceH2)
                .rowMapper(new BeanPropertyRowMapper<>(InputDTO.class)) // equivalent to .beanRowMapper(InputDTO.class)
                .pageSize(1000)
                .fetchSize(1000)
                .queryProvider(queryProvider)
                .build();

        // MAJOR FUCKED UP THING IN BUILDER
        // BUILDER SHOULD EITHER CALL THIS BAD DESIGN CHOICE "afterPropertiesSet"
        // OR SHOULD SET namedParameterJdbcTemplate to something
        try {
            jdbcPagingItemReader.afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return jdbcPagingItemReader;
    }

}
