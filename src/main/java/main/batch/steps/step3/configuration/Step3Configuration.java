package main.batch.steps.step3.configuration;

import main.batch.listeners.CustomChunkListener;
import main.batch.listeners.CustomStepListener;
import main.batch.steps.step3.processors.Step3ItemProcessor;
import main.databases.mysql.domain.PersonMySQL;
import main.databases.postgresql.domain.PersonPostgreSQL;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;

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

    @Autowired
    TaskExecutor taskExecutor;

    @Bean
    public Step step3() throws Exception {
        return stepBuilderFactory.get("step3")
                .<PersonMySQL, PersonPostgreSQL>chunk(50)// check chunk usage
                .reader(step3ItemReader())
                .processor(step3ItemProcessor())
                .writer(step3ItemWriter())
                .transactionManager(platformTransactionManager)
                .listener(new CustomStepListener())
                .listener(new CustomChunkListener())
                .taskExecutor(taskExecutor)
                .throttleLimit(5)
                .build();
    }

    @Bean
    public ItemReader<PersonMySQL> step3ItemReader() throws Exception {
        JpaPagingItemReader<PersonMySQL> reader = new JpaPagingItemReader<>();
        String query = "select t from PersonMySQL t";
        reader.setEntityManagerFactory(mysqlEMF);
        reader.setPageSize(50);
        reader.setQueryString(query);
        reader.afterPropertiesSet();
        reader.setSaveState(true);
        return reader;
    }

    @Bean
    public ItemProcessor<PersonMySQL, PersonPostgreSQL> step3ItemProcessor() {
        return new Step3ItemProcessor();
    }

    @Bean
    public ItemWriter<PersonPostgreSQL> step3ItemWriter() throws Exception {
        JpaItemWriter<PersonPostgreSQL> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(postgresqlEMF);
        writer.afterPropertiesSet();
        return writer;
    }

    @Bean
    public TaskExecutor taskExecutor(){
        return new SimpleAsyncTaskExecutor("spring_batch_thread_executor");
    }

}
