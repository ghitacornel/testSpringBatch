package jdbc.configuration.hsql.repository;

import jdbc.configuration.hsql.entity.OutputEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonHSQLRepository extends JpaRepository<OutputEntity, Integer> {
}
