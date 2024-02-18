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
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class JobConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job(Step step) {
        return jobBuilderFactory.get("main.jobs.csv.parallel.JobConfiguration")
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    public Step step(ItemReader<InputData> reader, ItemProcessor<InputData, OutputData> processor, ItemWriter<OutputData> writer) {
        return stepBuilderFactory.get("main.jobs.csv.parallel.JobConfiguration.step")
                .<InputData, OutputData>chunk(10)// larger is faster but requires more memory
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                .throttleLimit(10)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<InputData, OutputData> processor(Validator validator) {
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
            output.setProcessingThread(Thread.currentThread().toString());
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
                        setNames(new String[]{"id", "firstName", "lastName", "age", "salary", "processingThread"});
                    }
                });
            }
        });

        return writer;
    }

    private TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor("parallelTaskExecutor");
    }
}
