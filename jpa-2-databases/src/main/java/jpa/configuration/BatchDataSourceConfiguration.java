package jpa.configuration;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Configuration
class BatchDataSourceConfiguration {

    @Value("${spring.datasource.batch.url}")
    private String url;

    @Value("${spring.datasource.batch.username}")
    private String username;

    @Value("${spring.datasource.batch.password}")
    private String password;

    @Value("${spring.datasource.batch.driver}")
    private String driver;

    @Value("${spring.datasource.batch.schema}")
    private String schema;

    // the default data source to be used by Spring Batch
    // Spring batch always needs at least 1 data source
    @Primary
    @Bean
    DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driver);
        if (StringUtils.hasText(schema)) {
            config.setSchema(schema);
        }
        return new HikariDataSource(config);
    }

}
