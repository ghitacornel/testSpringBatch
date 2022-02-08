package main.databases.hsql.repositories;

import main.databases.hsql.domain.PersonHSQL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonHSQLRepository extends JpaRepository<PersonHSQL, Integer> {
}
