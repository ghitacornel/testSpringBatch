package main.databases.mysql.repositories;

import main.databases.mysql.domain.PersonMySQL;
import org.springframework.data.repository.CrudRepository;

public interface PersonMySQLRepository extends CrudRepository<PersonMySQL, Integer> {
}
