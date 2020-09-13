package main.batch.steps.step0.configuration;

import com.github.javafaker.Faker;
import main.batch.listeners.CustomStepListener;
import main.batch.steps.step0.model.DummyData;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class Step0Configuration {

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Value("${step1.input.file}")
    String step0OutputFile;

    private Integer count = 0;

    @Bean
    public Step step0() {
        return stepBuilderFactory.get("step0")
                .<DummyData, DummyData>chunk(100)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .listener(new CustomStepListener())
                .build();
    }

    private ItemReader<DummyData> reader() {
        return () -> {
            if (count > 1000) return null;
            count++;
            DummyData dummyData = new DummyData();
            dummyData.setId(count);
            return dummyData;
        };
    }

    private ItemProcessor<DummyData, DummyData> processor() {
        return item -> {
            item.setAge(new Faker().number().numberBetween(20, 50));
            item.setSalary(new Faker().number().randomDouble(2, 500, 5000));
            item.setName(new Faker().name().fullName());
            return item;
        };
    }

    private FlatFileItemWriter<DummyData> writer() {
        return new FlatFileItemWriterBuilder<DummyData>()
                .resource(new FileSystemResource(step0OutputFile))
                .append(true)
                .lineAggregator(new DelimitedLineAggregator<>() {
                    {
                        setDelimiter(",");
                        setFieldExtractor(new BeanWrapperFieldExtractor<>() {
                            {
                                setNames(new String[]{"id", "name", "salary", "age"});
                            }
                        });
                    }
                })
                .name("step 0 item writer")
                .build();
    }

}
