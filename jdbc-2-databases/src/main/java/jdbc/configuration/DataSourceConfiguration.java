package jdbc.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
class DataSourceConfiguration {

    // the default data source to be used by Spring Batch
    // Spring batch always needs at least 1 data source
    @Primary
    @Bean
    DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.apache.derby.iapi.jdbc.AutoloadedDriver");
        config.setJdbcUrl("jdbc:derby:memory:demo;create=true");
        return new HikariDataSource(config);
    }

    @Bean
    DataSource dataSourceH2() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:mem:test");
        return new HikariDataSource(config);
    }

    @Bean
    DataSource dataSourceHSQL() {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.hsqldb.jdbcDriver");
        config.setJdbcUrl("jdbc:hsqldb:mem:testdb");
        return new HikariDataSource(config);
    }

}
