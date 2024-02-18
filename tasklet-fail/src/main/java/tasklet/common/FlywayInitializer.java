package tasklet.common;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class FlywayInitializer {

    private final DataSource dataSource;

    @PostConstruct
    public void migrateFlyway() {

        ClassicConfiguration configuration = new ClassicConfiguration();
        configuration.setDataSource(dataSource);
        configuration.setLocationsAsStrings("db/migration/batch");
        configuration.setBaselineOnMigrate(true);
        Flyway flyway = new Flyway(configuration);
        flyway.migrate();

    }
}