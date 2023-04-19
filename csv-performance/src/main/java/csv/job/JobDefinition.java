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
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class JobDefinition {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job job(@Qualifier("step") Step step) {
        return jobBuilderFactory.get("main.jobs.csv.performance.JobDefinition")
                .incrementer(new RunIdIncrementer())
                .start(createData())
                .next(step)
                .next(verifyFile())
                .build();
    }

    private Step createData() {
        return stepBuilderFactory
                .get("main.jobs.csv.performance.JobDefinition.createData")
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
                })
                .build();
    }

    private Step verifyFile() {
        return stepBuilderFactory
                .get("main.jobs.csv.performance.JobDefinition.verifyFile")
                .tasklet((contribution, chunkContext) -> {
                    String inputPath = (String) chunkContext.getStepContext().getJobParameters().get("outputPath");
                    long count = (long) chunkContext.getStepContext().getJobParameters().get("count");
                    List<String> allLines = Files.readAllLines(Paths.get(inputPath));
                    if (allLines.size() != count) {
                        throw new RuntimeException("expected " + count + " found " + allLines.size());
                    }
                    return RepeatStatus.FINISHED;
                })
                .build();
    }

    @Bean
    public Step step(ItemReader<InputData> reader, ItemProcessor<InputData, OutputData> processor, ItemWriter<OutputData> writer) {
        return stepBuilderFactory.get("main.jobs.csv.performance.JobDefinition.step")
                .<InputData, OutputData>chunk(1000)// larger is faster but requires more memory
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .taskExecutor(taskExecutor())
                .throttleLimit(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<InputData, OutputData> processor() {
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
    public FlatFileItemReader<InputData> reader(@Value("#{jobParameters['inputPath']}") String inputPath) {
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
    public FlatFileItemWriter<OutputData> writer(@Value("#{jobParameters['outputPath']}") String outputPath) {
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
        return new SimpleAsyncTaskExecutor("performanceTaskExecutor");
    }

}
