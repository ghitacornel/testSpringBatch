package main.jobs.csvs;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;

@Profile("main.jobs.csvs.Job1ReadWrite")
@Configuration
public class Job1ReadWrite {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job Job1ReadWrite(@Qualifier("main.jobs.csvs.Job1ReadWrite.step") Step step) {
        return jobBuilderFactory.get("main.jobs.csvs.Job1ReadWrite")
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean("main.jobs.csvs.Job1ReadWrite.step")
    @StepScope
    public Step step(@Qualifier("main.jobs.csvs.Job1ReadWrite.step.reader") ItemReader<Employee> reader,
                     @Qualifier("main.jobs.csvs.Job1ReadWrite.step.writer") ItemWriter<Employee> writer) {
        return stepBuilderFactory.get("main.jobs.csvs.Job1ReadWrite.step").<Employee, Employee>chunk(5)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean("main.jobs.csvs.Job1ReadWrite.step.reader")
    @StepScope
    public FlatFileItemReader<Employee> reader(@Value("#{jobParameters['inputPath']}") String inputPath) {
        FlatFileItemReader<Employee> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputPath));
        reader.setLinesToSkip(1);
        reader.setLineMapper(new DefaultLineMapper<>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames("id", "firstName", "lastName");
                    }
                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(Employee.class);
                    }
                });
            }
        });
        return reader;
    }

    @Bean("main.jobs.csvs.Job1ReadWrite.step.writer")
    @StepScope
    public FlatFileItemWriter<Employee> writer(@Value("#{jobParameters['outputPath']}") String outputPath) {
        FlatFileItemWriter<Employee> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputPath));
        writer.setAppendAllowed(false);
        writer.setLineAggregator(new DelimitedLineAggregator<>() {
            {
                setDelimiter(",");
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[]{"id", "firstName", "lastName"});
                    }
                });
            }
        });
        return writer;
    }


}
