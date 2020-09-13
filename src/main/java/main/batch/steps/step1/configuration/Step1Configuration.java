package main.batch.steps.step1.configuration;

import main.batch.listeners.CustomStepListener;
import main.batch.steps.step1.model.Step1InputDataModel;
import main.batch.steps.step1.model.Step1OutputDataModel;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
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
                .reader(new FlatFileItemReaderBuilder<Step1InputDataModel>()
                        .name("step 1 reader")
                        .resource(new FileSystemResource(step1InputFile))
                        .delimited()
                        .names(new String[]{"id", "name", "salary", "age"})
                        .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                            setTargetType(Step1InputDataModel.class);
                        }})
                        .strict(true)
                        .build())
                .processor((ItemProcessor<Step1InputDataModel, Step1OutputDataModel>) input -> {
                    Step1OutputDataModel output = new Step1OutputDataModel();
                    output.setId(input.getId());
                    output.setName(input.getName());
                    output.setSalary(input.getSalary());
                    output.setAge(input.getAge());
                    return output;
                })
                .writer(new JsonFileItemWriterBuilder<Step1OutputDataModel>()
                        .name("step 1 writer")
                        .jsonObjectMarshaller(new JacksonJsonObjectMarshaller<>())
                        .resource(new FileSystemResource(step1OutputFile))
                        .append(false)
                        .build())
                .listener(new CustomStepListener())
                .build();
    }

}
