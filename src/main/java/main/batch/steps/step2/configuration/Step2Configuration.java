package main.batch.steps.step2.configuration;

import main.batch.listeners.CustomStepListener;
import main.batch.steps.step2.model.Step2InputDataModel;
import main.databases.mysql.domain.PersonMySQL;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
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
    public Step step2() {
        return stepBuilderFactory.get("step2")
                .<Step2InputDataModel, PersonMySQL>chunk(100)// check chunk usage
                .reader(new JsonItemReaderBuilder<Step2InputDataModel>()
                        .jsonObjectReader(new JacksonJsonObjectReader<>(Step2InputDataModel.class))
                        .resource(new FileSystemResource(step2InputFile))
                        .name("step 2 reader")
                        .build())
                .processor((ItemProcessor<Step2InputDataModel, PersonMySQL>) input -> {
                    PersonMySQL output = new PersonMySQL();
                    output.setId(input.getId());
                    output.setName(input.getName());
                    output.setSalary(input.getSalary());
                    output.setAge(input.getAge());
                    return output;
                })
                .writer(new JpaItemWriterBuilder<PersonMySQL>()
                        .entityManagerFactory(entityManagerFactory)
                        .build())
                .transactionManager(platformTransactionManager)
                .listener(new CustomStepListener())
                .build();
    }

}
