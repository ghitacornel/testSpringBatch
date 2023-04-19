package jdbc.configuration.hsql.repository;

import jdbc.configuration.hsql.entity.PersonHSQL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonHSQLRepository extends JpaRepository<PersonHSQL, Integer> {
}
