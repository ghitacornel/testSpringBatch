package jpa.configuration.output;

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
        entityManagerFactoryRef = "outputEntityManager",
        basePackages = {"jpa.configuration.output"}
)
@DependsOn({"flywayInitializer"})
class OutputConfiguration {

    @Bean
    LocalContainerEntityManagerFactoryBean outputEntityManager(@Qualifier("outputDataSource") DataSource dataSource, EntityManagerFactoryBuilder builder) {
        Map<String, String> map = new HashMap<>();
        map.put("hibernate.show_sql", "false");
        map.put("hibernate.format_sql", "false");
        map.put("javax.persistence.schema-generation.database.action", "validate");
        map.put("hibernate.default_schema", "output");
        return builder
                .dataSource(dataSource)
                .packages("jpa.configuration.output.entity")
                .persistenceUnit("outputPU")
                .properties(map)
                .build();
    }

}
