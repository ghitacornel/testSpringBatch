package jdbc.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

@Configuration
@RequiredArgsConstructor
class JobJdbcReadWritePerformanceSingleThreadConfiguration {

    @Qualifier("dataSourceH2")
    private final DataSource dataSourceH2;

    @Qualifier("dataSourceHSQL")
    private final DataSource dataSourceHSQL;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    Job jobJdbcReadWritePerformanceSingleThread() {
        // only a count is performed as validation
        return new JobBuilder("jobJdbcReadWritePerformanceSingleThread", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(new StepBuilder("createDataStep", jobRepository)
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
                            List<InputDTO> list = InputGenerator.generate(count);

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
                        }, transactionManager)
                        .build())
                .next(new StepBuilder("processingStep", jobRepository)

                        // larger is faster but requires more memory
                        .<InputDTO, OutputDTO>chunk(1000, transactionManager)

                        // reader/EXTRACT
                        .reader(new JdbcCursorItemReaderBuilder<InputDTO>()
                                .name("jdbcCursorItemReader")
                                .dataSource(dataSourceH2)
                                .sql("select * from InputDTO")// programmer is responsible for filtering the data to be processed
                                .rowMapper(new BeanPropertyRowMapper<>(InputDTO.class))
                                .build())

                        // processor/TRANSFORM
                        .processor(input -> {
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
                        .build())
                .next(new StepBuilder("verifyDatabaseStep", jobRepository)
                        .tasklet((contribution1, chunkContext1) -> {
                            Connection connection1 = dataSourceHSQL.getConnection();
                            PreparedStatement preparedStatement1 = connection1.prepareStatement("select count(*) from OutputDTO");
                            ResultSet resultSet = preparedStatement1.executeQuery();
                            resultSet.next();
                            long actualCount = resultSet.getLong(1);
                            preparedStatement1.close();
                            connection1.close();
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
