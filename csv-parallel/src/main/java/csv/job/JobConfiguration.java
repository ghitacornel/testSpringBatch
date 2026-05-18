package csv.job;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.FlatFileItemWriter;
import org.springframework.batch.infrastructure.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.infrastructure.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.infrastructure.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineAggregator;
import org.springframework.batch.infrastructure.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.AsyncTaskExecutor;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;

import java.util.Set;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
class JobConfiguration {

    private final JobRepository jobRepository;

    @Bean
    Job job(Step step) {
        return new JobBuilder("main.jobs.csv.parallel.JobConfiguration", jobRepository)
                .start(step)
                .build();
    }

    @Bean
    Step step(ItemReader<InputData> reader, ItemProcessor<InputData, OutputData> processor, ItemWriter<OutputData> writer) {
        return new StepBuilder("main.jobs.csv.parallel.JobConfiguration.step", jobRepository)
                .<InputData, OutputData>chunk(10)// larger is faster but requires more memory
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    @StepScope
    ItemProcessor<InputData, OutputData> processor(Validator validator) {
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
    FlatFileItemReader<InputData> reader(@Value("#{jobParameters['inputPath']}") String inputPath) {
        FlatFileItemReader<InputData> reader = new FlatFileItemReader<>(
                new FileSystemResource(inputPath),
                new DefaultLineMapper<>() {
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
        reader.setLinesToSkip(1);// skip header
        return reader;
    }

    @Bean
    @StepScope
    FlatFileItemWriter<OutputData> writer(@Value("#{jobParameters['outputPath']}") String outputPath) {
        return new FlatFileItemWriter<>(
                new FileSystemResource(outputPath),
                new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(",");// can specify custom delimiter here
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "firstName", "lastName", "age", "salary", "processingThread"});
                            }
                        });
                    }
                });
    }

    private AsyncTaskExecutor taskExecutor() {
        return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(10));
    }
}
