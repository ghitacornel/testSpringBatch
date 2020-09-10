package main.batch.steps.step1.configuration;

import main.batch.listeners.CustomStepListener;
import main.batch.steps.step1.model.Step1InputDataModel;
import main.batch.steps.step1.model.Step1OutputDataModel;
import main.batch.steps.step1.processors.Step1ItemProcessor;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.builder.JsonFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

@Configuration
public class Step1Configuration {

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Value("${step1.input.file}")
    String step1InputFile;

    @Value("${step1.output.file}")
    String step1OutputFile;

    @Bean
    public Step step1() {
        return stepBuilderFactory.get("step1")
                .<Step1InputDataModel, Step1OutputDataModel>chunk(100)
                .reader(step1ItemReader())
                .processor(step1ItemProcessor())
                .writer(step1ItemWriter())
                .listener(new CustomStepListener())
                .build();
    }

    @Bean
    public ItemReader<Step1InputDataModel> step1ItemReader() {
        return new FlatFileItemReaderBuilder<Step1InputDataModel>()
                .name("step1ItemReader")
                .resource(new FileSystemResource(step1InputFile))
                .delimited()
                .names(new String[]{"id", "name", "salary", "age"})
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(Step1InputDataModel.class);
                }})
                .linesToSkip(1)
                .strict(true)
                .build();
    }

    @Bean
    public ItemProcessor<Step1InputDataModel, Step1OutputDataModel> step1ItemProcessor() {
        return new Step1ItemProcessor();
    }

    @Bean
    public ItemWriter<Step1OutputDataModel> step1ItemWriter() {
        return new JsonFileItemWriterBuilder<Step1OutputDataModel>()
                .name("step1ItemWriter")
                .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                .resource(new FileSystemResource(step1OutputFile))
                .build();
    }

}
