package main.jobs.csv;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;

@Profile("main.jobs.csv.JobCsvReadWrite")
@Configuration
public class JobCsvReadWrite {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job(Step step) {
        return jobBuilderFactory.get("main.jobs.csv.JobCsvReadWrite")
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    @StepScope
    public Step step(ItemReader<InputItem> reader, ItemProcessor<InputItem, OutputItem> processor, ItemWriter<OutputItem> writer) {
        return stepBuilderFactory.get("main.jobs.csv.JobCsvReadWrite.step").<InputItem, OutputItem>chunk(5)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<InputItem, OutputItem> processor() {
        return input -> {
            OutputItem output = new OutputItem();
            output.setId(input.getId());
            output.setFirstName(input.getFirstName());
            output.setLastName(input.getLastName());
            output.setAge(input.getAge() + 1);
            output.setSalary(input.getSalary() + 2);
            output.setDifference(input.getSalary() - input.getAge());
            return output;
        };
    }

    @Bean
    @StepScope
    public FlatFileItemReader<InputItem> reader(@Value("#{jobParameters['inputPath']}") String inputPath) {
        FlatFileItemReader<InputItem> reader = new FlatFileItemReader<>();
        reader.setResource(new FileSystemResource(inputPath));
        reader.setLinesToSkip(1);// skip header
        reader.setLineMapper(new DefaultLineMapper<>() {
            {
                setLineTokenizer(new DelimitedLineTokenizer() {
                    {
                        setNames("id", "firstName", "lastName", "age", "salary");
                    }
                });
                setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {
                    {
                        setTargetType(InputItem.class);
                    }
                });
            }
        });
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<OutputItem> writer(@Value("#{jobParameters['outputPath']}") String outputPath) {
        FlatFileItemWriter<OutputItem> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputPath));
        writer.setAppendAllowed(false);// append or rewrite
        writer.setLineAggregator(new DelimitedLineAggregator<>() {
            {
                setDelimiter(";");
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[]{"id", "firstName", "lastName", "age", "salary", "difference"});
                    }
                });
            }
        });
        return writer;
    }


}
