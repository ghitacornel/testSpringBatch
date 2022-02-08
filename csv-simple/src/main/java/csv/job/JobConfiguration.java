package csv.job;

import lombok.RequiredArgsConstructor;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final Validator validator;

    @Bean
    public Job job(Step step) {
        return jobBuilderFactory.get("main.jobs.csv.JobConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(ItemReader<InputData> reader, ItemProcessor<InputData, OutputData> processor, ItemWriter<OutputData> writer) {
        return stepBuilderFactory.get("main.jobs.csv.JobConfiguration.step")
                .<InputData, OutputData>chunk(100)// larger is faster but requires more memory
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<InputData, OutputData> processor() {
        return input -> {
            {
                Set<ConstraintViolation<InputData>> violations = validator.validate(input);
                if (!violations.isEmpty()) {
                    System.err.println(violations);
                    return null;
                }
            }
            OutputData output = new OutputData();
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
    public FlatFileItemReader<InputData> reader(@Value("#{jobParameters['inputPath']}") String inputPath) {
        FlatFileItemReader<InputData> reader = new FlatFileItemReader<>();
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
                        setTargetType(InputData.class);
                    }
                });
            }
        });
        return reader;
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<OutputData> writer(@Value("#{jobParameters['outputPath']}") String outputPath) {
        FlatFileItemWriter<OutputData> writer = new FlatFileItemWriter<>();
        writer.setResource(new FileSystemResource(outputPath));

        // append or rewrite
        writer.setAppendAllowed(false);

        // delimiter and header order
        writer.setLineAggregator(new DelimitedLineAggregator<>() {
            {
                setDelimiter(",");// can specify custom delimiter here
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[]{"id", "firstName", "lastName", "age", "salary", "difference"});
                    }
                });
            }
        });

        // custom header labels
        writer.setHeaderCallback(writer1 -> writer1.write("header1,header2,header3,header4,header5,header6"));

        // custom end of file
        writer.setFooterCallback(writer12 -> writer12.write("done"));

        return writer;
    }


}
