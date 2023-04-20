package jdbc.configuration;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class FlywayInitializer {


    private final DataSource dataSource;
    private final DataSource dataSourceH2;
    private final DataSource dataSourceHSQL;

    @PostConstruct
    public void migrateFlyway() {

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