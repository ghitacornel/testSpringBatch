package jpa.configuration.input;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "inputEntityManager",
        basePackages = {"jpa.configuration.input"}
)
@DependsOn({"flywayInitializer"})
class InputConfiguration {

    @Bean
    LocalContainerEntityManagerFactoryBean inputEntityManager(@Qualifier("inputDataSource") DataSource dataSource, EntityManagerFactoryBuilder builder) {
        Map<String, String> map = new HashMap<>();
        map.put("hibernate.show_sql", "false");
        map.put("hibernate.format_sql", "false");
        map.put("javax.persistence.schema-generation.database.action", "validate");
        map.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        return builder
                .dataSource(dataSource)
                .packages("jpa.configuration.input.entity")
                .persistenceUnit("inputPU")
                .properties(map)
                .build();
    }

}
