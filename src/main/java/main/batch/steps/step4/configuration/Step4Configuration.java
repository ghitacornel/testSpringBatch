package main.batch.steps.step4.configuration;

import main.batch.listeners.CustomStepListener;
import main.batch.steps.step4.model.Step4OutputDataModel;
import main.batch.steps.step4.processors.Step4ItemProcessor;
import main.databases.postgresql.domain.PersonPostgreSQL;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class Step4Configuration {

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Value("${step4.output.file}")
    String step4OutputFile;

    @Autowired
    @Qualifier("dataSourcePostgreSQL")
    DataSource dataSource;

    @Autowired
    Jaxb2Marshaller marshaller;

    @Bean
    public Step step4() {
        return stepBuilderFactory.get("step4")
                .<PersonPostgreSQL, Step4OutputDataModel>chunk(100)
                .reader(step4ItemReader())
                .processor(step4ItemProcessor())
                .writer(step4ItemWriter())
                .listener(new CustomStepListener())
                .build();
    }

    @Bean
    public ItemReader<PersonPostgreSQL> step4ItemReader() {
        return new JdbcCursorItemReaderBuilder<PersonPostgreSQL>()
                .name("step4ItemReader")
                .dataSource(dataSource)
                .sql("select * from postgresql_database.person_postgresql")
                .fetchSize(1)
                .rowMapper(new BeanPropertyRowMapper<>(PersonPostgreSQL.class))
                .build();
    }

    @Bean
    public ItemProcessor<PersonPostgreSQL, Step4OutputDataModel> step4ItemProcessor() {
        return new Step4ItemProcessor();
    }

    @Bean
    public ItemWriter<Step4OutputDataModel> step4ItemWriter() {
        StaxEventItemWriter<Step4OutputDataModel> staxEventItemWriter = new StaxEventItemWriter<>();
        staxEventItemWriter.setResource(new FileSystemResource(step4OutputFile));
        staxEventItemWriter.setMarshaller(marshaller);
        staxEventItemWriter.setRootTagName("root-tag-name");
        return staxEventItemWriter;
    }

    @Bean
    public Jaxb2Marshaller marshaller() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Step4OutputDataModel.class);
        Map<String, Object> map = new HashMap<>();
//        map.put("jaxb.formatted.output", true);
//        map.put(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setMarshallerProperties(map);
        marshaller.afterPropertiesSet();
        return marshaller;
    }

}
