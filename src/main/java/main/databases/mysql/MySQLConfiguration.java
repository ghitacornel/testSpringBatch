package main.databases.mysql;

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
@EnableJpaRepositories(entityManagerFactoryRef = "mysqlEMF",
        transactionManagerRef = "mysqlPTM", basePackages = {"main.databases.mysql.repositories"})
public class MySQLConfiguration {

    @Autowired
    @Qualifier("dataSource1")
    DataSource dataSource;

    // no need for a dedicated EntityManagerFactory bean
    @Bean(name = "mysqlEMF")
    public LocalContainerEntityManagerFactoryBean mysqlEMF(EntityManagerFactoryBuilder builder) {
        Map<String, String> map = new HashMap<>();
//        map.put("javax.persistence.schema-generation.database.action", "drop-and-create");
//        map.put("javax.persistence.schema-generation.database.action", "validate");
        map.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        return builder
                .dataSource(dataSource)
                .packages("main.databases.mysql.domain")
                .persistenceUnit("mysql")
                .properties(map)
                .build();
    }

    @Bean(name = "mysqlPTM")
    public PlatformTransactionManager mysqlPTM(@Qualifier("mysqlEMF") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
