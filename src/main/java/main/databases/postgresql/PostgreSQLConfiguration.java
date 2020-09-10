package main.databases.postgresql;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableJpaRepositories(entityManagerFactoryRef = "postgresqlEMF",
        transactionManagerRef = "postgresqlPTM", basePackages = {"main.databases.postgresql.repositories"})
public class PostgreSQLConfiguration {

    @Autowired
    @Qualifier("dataSourcePostgreSQL")
    DataSource dataSource;

    // no need for a dedicated EntityManagerFactory bean
    @Bean(name = "postgresqlEMF")
    public LocalContainerEntityManagerFactoryBean postgresqlEMF(EntityManagerFactoryBuilder builder) {
        Map<String, String> map = new HashMap<>();
//        map.put("javax.persistence.schema-generation.database.action", "drop-and-create");
        map.put("javax.persistence.schema-generation.database.action", "validate");
        return builder
                .dataSource(dataSource)
                .packages("main.databases.postgresql.domain")
                .persistenceUnit("postgresql")
                .properties(map)
                .build();
    }

    @Bean(name = "postgresqlPTM")
    public PlatformTransactionManager postgresqlPTM(@Qualifier("postgresqlEMF") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
