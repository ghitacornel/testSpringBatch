package main.databases.postgresql.repositories;

import main.databases.postgresql.domain.PersonPostgreSQL;
import org.springframework.data.repository.CrudRepository;

public interface PersonPostgreSQLRepository extends CrudRepository<PersonPostgreSQL, Integer> {
}
