package main.jobs.jdbc.performance;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

@Profile("main.jobs.jdbc.performance.JobJdbcReadWritePerformance")
@Configuration
public class JobJdbcReadWritePerformance {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("dataSource1")
    DataSource dataSource1;

    @Autowired
    @Qualifier("dataSource2")
    DataSource dataSource2;

    @Bean
    public Job job(@Qualifier("createData") Step createData, @Qualifier("step") Step step, @Qualifier("verifyFile") Step verifyFile) {
        return jobBuilderFactory.get("main.jobs.jdbc.performance.JobJdbcReadWritePerformance")
                .incrementer(new RunIdIncrementer())
                .start(createData)
                .next(step)
                .next(verifyFile)
                .build();
    }

    @Bean
    public Step createData() {
        return stepBuilderFactory
                .get("main.jobs.jdbc.performance.JobJdbcReadWritePerformance.createData")
                .tasklet((contribution, chunkContext) -> {
//                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    long count = 1000;
                    List<InputDTO> list = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        InputDTO inputDTO = InputDTO.generate();
                        list.add(inputDTO);
                    }
                    Connection connection = dataSource1.getConnection();
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

    @Bean
    public Step verifyFile() {
        return stepBuilderFactory
                .get("main.jobs.jdbc.performance.JobJdbcReadWritePerformance.verifyFile")
                .tasklet((contribution, chunkContext) -> {
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    Connection connection = dataSource2.getConnection();
                    PreparedStatement preparedStatement = connection.prepareStatement("select count(*) from OutputDTO");
                    ResultSet resultSet = preparedStatement.executeQuery();
                    resultSet.next();
                    long actualCount = resultSet.getLong(1);
                    preparedStatement.close();
                    connection.close();
                    if (actualCount != count) {
                        throw new RuntimeException("expected " + count + " found " + actualCount);
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }


    @Bean
    public Step step(ItemReader<InputDTO> reader, ItemProcessor<InputDTO, OutputDTO> processor, ItemWriter<OutputDTO> writer, TaskExecutor taskExecutor) {
        return stepBuilderFactory.get("main.jobs.jdbc.performance.JobJdbcReadWritePerformance.step")
                .<InputDTO, OutputDTO>chunk(1000)// larger is faster but requires more memory
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor)
                .throttleLimit(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<InputDTO, OutputDTO> processor() {
        return input -> {
            OutputDTO output = new OutputDTO();
            output.setId(input.getId());
            output.setFirstName(input.getFirstName());
            output.setLastName(input.getLastName());
            output.setAge(input.getAge() + 1);
            output.setSalary(input.getSalary() + 2);
            output.setDifference(output.getSalary() - output.getAge());
            return output;
        };
    }

    @Bean
    @StepScope
    public JdbcCursorItemReader<InputDTO> reader() {
        return new JdbcCursorItemReaderBuilder<InputDTO>()
                .name("cursorItemReader")
                .dataSource(dataSource1)
                .sql("select * from InputDTO")
                .rowMapper(new BeanPropertyRowMapper<>(InputDTO.class))
                .build();
    }

    @Bean
    @StepScope
    public JdbcBatchItemWriter<OutputDTO> writer() {
        return new JdbcBatchItemWriterBuilder<OutputDTO>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO OutputDTO(id,firstName,lastName,salary,age,difference) VALUES (:id,:firstName,:lastName,:salary,:age,:difference")
                .dataSource(dataSource2)
                .build();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("performanceTaskExecutor");
    }

}
