package jdbc.common.hsql.repositories;

import jdbc.common.hsql.domain.PersonHSQL;
import main.databases.hsql.domain.PersonHSQL;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonHSQLRepository extends JpaRepository<PersonHSQL, Integer> {
}
