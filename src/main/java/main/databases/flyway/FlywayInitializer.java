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
    @Qualifier("dataSourceMySQL")
    private DataSource dataSourceMySQL;

    @Autowired
    @Qualifier("dataSourcePostgreSQL")
    private DataSource dataSourcePostgreSQL;

    @PostConstruct
    public void migrateFlyway() {

        {
            ClassicConfiguration configuration = new ClassicConfiguration();
            configuration.setDataSource(dataSource);
            configuration.setLocationsAsStrings("db/migration/batch");
            configuration.setSchemas("batch_database");
            configuration.setBaselineOnMigrate(true);
            Flyway flyway = new Flyway(configuration);
            flyway.migrate();
        }

        {
            ClassicConfiguration configuration = new ClassicConfiguration();
            configuration.setDataSource(dataSourceMySQL);
            configuration.setLocationsAsStrings("db/migration/mysql");
            configuration.setSchemas("mysql_database");
            configuration.setBaselineOnMigrate(true);
            Flyway flyway = new Flyway(configuration);
            flyway.migrate();
        }

        {
            ClassicConfiguration configuration = new ClassicConfiguration();
            configuration.setDataSource(dataSourcePostgreSQL);
            configuration.setLocationsAsStrings("db/migration/postgres");
            configuration.setSchemas("postgresql_database");
            configuration.setBaselineOnMigrate(true);
            Flyway flyway = new Flyway(configuration);
            flyway.migrate();
        }

    }
}