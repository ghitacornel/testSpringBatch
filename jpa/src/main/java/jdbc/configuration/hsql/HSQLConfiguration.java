package jdbc.configuration.hsql;

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
        entityManagerFactoryRef = "hsqlEMFB",
        transactionManagerRef = "hsqlPTM",
        basePackages = {"jdbc.configuration.hsql"}
)
@DependsOn({"flywayInitializer"})
public class HSQLConfiguration {

    @Autowired
    @Qualifier("dataSourceHSQL")
    private DataSource dataSource;

    @Bean
    LocalContainerEntityManagerFactoryBean hsqlEMFB(EntityManagerFactoryBuilder builder) {
        Map<String, String> map = new HashMap<>();
        map.put("hibernate.show_sql", "true");
        map.put("hibernate.format_sql", "true");
        map.put("javax.persistence.schema-generation.database.action", "validate");
        map.put("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        return builder
                .dataSource(dataSource)
                .packages("jdbc.configuration.hsql.entity")
                .persistenceUnit("hsql")
                .properties(map)
                .build();
    }

    @Bean
    PlatformTransactionManager hsqlPTM(@Qualifier("hsqlEMFB") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
