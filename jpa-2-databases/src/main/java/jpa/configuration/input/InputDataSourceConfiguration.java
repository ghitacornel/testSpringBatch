package jpa.configuration.input;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
class InputDataSourceConfiguration {

    @Value("${spring.datasource.input.url}")
    private String url;

    @Value("${spring.datasource.input.username}")
    private String username;

    @Value("${spring.datasource.input.password}")
    private String password;

    @Value("${spring.datasource.input.driver}")
    private String driver;

    @Value("${spring.datasource.input.schema}")
    private String schema;

    @Bean
    DataSource inputDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver);
        if(!schema.isBlank()) {
            config.setSchema(schema);
        }
        return new HikariDataSource(config);
    }

}
