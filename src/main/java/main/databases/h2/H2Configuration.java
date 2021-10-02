package main.databases.h2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "h2EMFB",
        transactionManagerRef = "h2PTM",
        basePackages = {"main.databases.h2.repositories"}
)
@DependsOn({"flywayInitializer"})
public class H2Configuration {

    @Autowired
    @Qualifier("dataSourceH2")
    DataSource dataSource;

    @Bean
    LocalContainerEntityManagerFactoryBean h2EMFB(EntityManagerFactoryBuilder builder) {
        Map<String, String> map = new HashMap<>();
//        map.put("javax.persistence.schema-generation.database.action", "drop-and-create");
        map.put("javax.persistence.schema-generation.database.action", "validate");
        map.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        return builder
                .dataSource(dataSource)
                .packages("main.databases.h2.domain")
                .persistenceUnit("h2")
                .properties(map)
                .build();
    }

    @Bean
    PlatformTransactionManager h2PTM(@Qualifier("h2EMFB") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
