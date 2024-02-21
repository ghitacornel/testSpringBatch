package csv.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Set;

@Slf4j
@Configuration
@RequiredArgsConstructor
class JobConfiguration {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    Job job(Step step) {
        return new JobBuilder("main.jobs.csv.JobConfiguration", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }

    @Bean
    Step step(ItemReader<InputData> reader, ItemProcessor<InputData, OutputData> processor, ItemWriter<OutputData> writer) {
        return new StepBuilder("main.jobs.csv.JobConfiguration.step", jobRepository)
                .<InputData, OutputData>chunk(100, transactionManager)// larger is faster but requires more memory
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    ItemProcessor<InputData, OutputData> processor(Validator validator) {
        return input -> {
            {
                Set<ConstraintViolation<InputData>> violations = validator.validate(input);
                if (!violations.isEmpty()) {
                    log.error(violations.toString());
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
    FlatFileItemReader<InputData> reader(@Value("#{jobParameters['inputPath']}") String inputPath) {
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
    FlatFileItemWriter<OutputData> writer(@Value("#{jobParameters['outputPath']}") String outputPath) {
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
