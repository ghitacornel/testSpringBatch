package jpa.configuration;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
class FlywayInitializer {

    @Qualifier("dataSource")
    private final DataSource dataSource;

    @Qualifier("dataSourceH2")
    private final DataSource dataSourceH2;

    @Qualifier("dataSourceHSQL")
    private final DataSource dataSourceHSQL;

    @PostConstruct
    void migrateFlyway() {

        {
            ClassicConfiguration configuration = new ClassicConfiguration();
            configuration.setDataSource(dataSource);
            configuration.setLocationsAsStrings("db/migration/batch");
            configuration.setBaselineOnMigrate(true);
            Flyway flyway = new Flyway(configuration);
            flyway.migrate();
        }

        {
            ClassicConfiguration configuration = new ClassicConfiguration();
            configuration.setDataSource(dataSourceH2);
            configuration.setLocationsAsStrings("db/migration/h2");
            configuration.setBaselineOnMigrate(true);
            Flyway flyway = new Flyway(configuration);
            flyway.migrate();
        }

        {
            ClassicConfiguration configuration = new ClassicConfiguration();
            configuration.setDataSource(dataSourceHSQL);
            configuration.setLocationsAsStrings("db/migration/hsql");
            configuration.setBaselineOnMigrate(true);
            Flyway flyway = new Flyway(configuration);
            flyway.migrate();
        }

    }
}