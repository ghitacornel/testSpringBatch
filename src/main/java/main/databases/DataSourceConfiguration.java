package main.databases;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfiguration {

    // the default data source to be used by Spring Batch
    // Spring batch always needs at least 1 data source
    @Primary
    @Bean(name = "dataSource")// mandatory name "dataSource"
    public DataSource dataSourceBatch() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mysql://localhost:3307/batch_database");
        dataSourceBuilder.username("cornel");
        dataSourceBuilder.password("sefusefu");
        return dataSourceBuilder.build();
    }

    @Bean
    public DataSource dataSourceMySQL() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("com.mysql.cj.jdbc.Driver");
        dataSourceBuilder.url("jdbc:mysql://localhost:3306/mysql_database");
        dataSourceBuilder.username("cornel");
        dataSourceBuilder.password("sefusefu");
        return dataSourceBuilder.build();
    }

    @Bean
    public DataSource dataSourcePostgreSQL() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName("org.postgresql.Driver");
        dataSourceBuilder.url("jdbc:postgresql://localhost:5432/postgresql_database");
        dataSourceBuilder.username("cornel");
        dataSourceBuilder.password("sefusefu");
        return dataSourceBuilder.build();
    }

}
