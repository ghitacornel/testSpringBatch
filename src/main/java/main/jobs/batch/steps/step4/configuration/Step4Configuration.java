package main.jobs.batch.steps.step4.configuration;

import main.jobs.batch.listeners.CustomStepListener;
import main.jobs.batch.steps.step4.model.Step4OutputDataModel;
import main.databases.postgresql.domain.PersonPostgreSQL;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
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
    @Qualifier("dataSource2")
    DataSource dataSource;

    @Bean
    public Step step4() throws Exception {
        return stepBuilderFactory.get("step4")
                .<PersonPostgreSQL, Step4OutputDataModel>chunk(100)
                .reader(new JdbcCursorItemReaderBuilder<PersonPostgreSQL>()
                        .dataSource(dataSource)
                        .sql("select * from postgresql_database.person_postgresql order by id")
                        .fetchSize(50)
                        .rowMapper(new BeanPropertyRowMapper<>(PersonPostgreSQL.class))
                        .name("step 4 reader")
                        .build())
                .processor((ItemProcessor<PersonPostgreSQL, Step4OutputDataModel>) input -> {
                    Step4OutputDataModel output = new Step4OutputDataModel();
                    output.setId(input.getId());
                    output.setName(input.getName());
                    output.setSalary(input.getSalary().doubleValue());
                    output.setAge(input.getAge());
                    return output;
                })
                .writer(new StaxEventItemWriterBuilder<Step4OutputDataModel>()
                        .resource(new FileSystemResource(step4OutputFile))
                        .marshaller(marshaller())
                        .rootTagName("root-tag-name")
                        .name("step 4 writer")
                        .build())
                .listener(new CustomStepListener())
                .build();
    }

    private static Jaxb2Marshaller marshaller() throws Exception {
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
