package csv.job;

import lombok.RequiredArgsConstructor;
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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ConcurrentTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
class JobDefinition {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    Job job(@Qualifier("step") Step step) {
        return new JobBuilder("main.jobs.csv.performance.JobDefinition", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(createData())
                .next(step)
                .next(verifyFile())
                .build();
    }

    private Step createData() {
        return new StepBuilder("main.jobs.csv.performance.JobDefinition.createData", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String inputPath = (String) chunkContext.getStepContext().getJobParameters().get("inputPath");
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    List<String> list = InputGenerator.generate(count);
                    FileWriter file = new FileWriter(inputPath);
                    for (String s : list) {
                        file.write(s);
                    }
                    file.flush();
                    file.close();
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    private Step verifyFile() {
        return new StepBuilder("main.jobs.csv.performance.JobDefinition.verifyFile", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    String inputPath = (String) chunkContext.getStepContext().getJobParameters().get("outputPath");
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    List<String> allLines = Files.readAllLines(Paths.get(inputPath));
                    if (allLines.size() != count) {
                        throw new RuntimeException("expected " + count + " found " + allLines.size());
                    }
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    Step step(ItemReader<InputData> reader, ItemProcessor<InputData, OutputData> processor, ItemWriter<OutputData> writer) {
        return new StepBuilder("main.jobs.csv.performance.JobDefinition.step", jobRepository)
                .<InputData, OutputData>chunk(1000, transactionManager)// larger is faster but requires more memory
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                .build();
    }

    @Bean
    @StepScope
    ItemProcessor<InputData, OutputData> processor() {
        return input -> {
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
        writer.setAppendAllowed(false);
        writer.setLineAggregator(new DelimitedLineAggregator<>() {
            {
                setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                    {
                        setNames(new String[]{"id", "firstName", "lastName", "age", "salary", "difference"});
                    }
                });
            }
        });
        return writer;
    }

    private TaskExecutor taskExecutor() {
        return new ConcurrentTaskExecutor(Executors.newFixedThreadPool(10));
    }

}
