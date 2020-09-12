package main.batch.steps.step2.configuration;

import main.batch.listeners.CustomStepListener;
import main.batch.steps.step2.model.Step2InputDataModel;
import main.batch.steps.step2.processors.Step2ItemProcessor;
import main.databases.mysql.domain.PersonMySQL;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

@Configuration
public class Step2Configuration {

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("mysqlEMF")
    EntityManagerFactory entityManagerFactory;

    @Autowired
    @Qualifier("mysqlPTM")
    PlatformTransactionManager platformTransactionManager;

    @Value("${step2.input.file}")
    String step2InputFile;

    @Bean
    public Step step2() throws Exception {
        return stepBuilderFactory.get("step2")
                .<Step2InputDataModel, PersonMySQL>chunk(100)// check chunk usage
                .reader(step2ItemReader())
                .processor(step2ItemProcessor())
                .writer(step2ItemWriter())
                .transactionManager(platformTransactionManager)
                .listener(new CustomStepListener())
                .build();
    }

    @Bean
    public ItemReader<Step2InputDataModel> step2ItemReader() {
        return new JsonItemReaderBuilder<Step2InputDataModel>()
                .jsonObjectReader(new JacksonJsonObjectReader(Step2InputDataModel.class))
                .resource(new FileSystemResource(step2InputFile))
                .name("step2ItemReader")
                .strict(true)
                .build();
    }

    @Bean
    public ItemProcessor<Step2InputDataModel, PersonMySQL> step2ItemProcessor() {
        return new Step2ItemProcessor();
    }

    @Bean
    public ItemWriter<PersonMySQL> step2ItemWriter() throws Exception {
        JpaItemWriter<PersonMySQL> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        writer.afterPropertiesSet();
        return writer;
    }

}
