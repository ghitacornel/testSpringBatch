package main.jobs.batch.steps.step3;

import main.jobs.batch.listeners.CustomChunkListener;
import main.jobs.batch.listeners.CustomStepListener;
import main.databases.mysql.domain.PersonMySQL;
import main.databases.postgresql.domain.PersonPostgreSQL;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import java.math.BigDecimal;

@Configuration
public class Step3Configuration {

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Autowired
    @Qualifier("mysqlEMF")
    EntityManagerFactory mysqlEMF;

    @Autowired
    @Qualifier("postgresqlEMF")
    EntityManagerFactory postgresqlEMF;

    @Autowired
    @Qualifier("postgresqlPTM")
    PlatformTransactionManager platformTransactionManager;

    @Bean
    public Step step3() {
        return stepBuilderFactory.get("step3")
                .<PersonMySQL, PersonPostgreSQL>chunk(50)// check chunk usage
                .reader(new JpaPagingItemReaderBuilder<PersonMySQL>()
                        .entityManagerFactory(mysqlEMF)
                        .pageSize(50)
                        .name("step 3 reader")
                        .queryString("select t from PersonMySQL t")
                        .build())
                .processor((ItemProcessor<PersonMySQL, PersonPostgreSQL>) inputDataModel -> {
                    PersonPostgreSQL outputDataModel = new PersonPostgreSQL();
                    outputDataModel.setId(inputDataModel.getId());
                    outputDataModel.setName(inputDataModel.getName());
                    outputDataModel.setSalary(BigDecimal.valueOf(inputDataModel.getSalary()));
                    outputDataModel.setAge(inputDataModel.getAge());
                    return outputDataModel;
                })
                .writer(new JpaItemWriterBuilder<PersonPostgreSQL>()
                        .entityManagerFactory(postgresqlEMF)
                        .build())
                .transactionManager(platformTransactionManager)
                .listener(new CustomStepListener())
                .listener(new CustomChunkListener())
                .taskExecutor(new SimpleAsyncTaskExecutor("spring_batch_thread_executor"))
                .throttleLimit(5)
                .build();
    }


}
