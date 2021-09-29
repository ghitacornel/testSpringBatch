package main.databases.flyway;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
public class FlywayInitializer {

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    @Autowired
    @Qualifier("dataSource1")
    private DataSource dataSource1;

    @Autowired
    @Qualifier("dataSource2")
    private DataSource dataSource2;

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
            configuration.setDataSource(dataSource1);
            configuration.setLocationsAsStrings("db/migration/mysql");
            configuration.setSchemas("mysql_database");
            configuration.setBaselineOnMigrate(true);
            Flyway flyway = new Flyway(configuration);
            flyway.migrate();
        }

        {
            ClassicConfiguration configuration = new ClassicConfiguration();
            configuration.setDataSource(dataSource2);
            configuration.setLocationsAsStrings("db/migration/postgres");
            configuration.setBaselineOnMigrate(true);
            Flyway flyway = new Flyway(configuration);
            flyway.migrate();
        }

    }
}