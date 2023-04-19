package jdbc.configuration.hsql.repository;

import jdbc.configuration.hsql.entity.OutputEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutputEntityRepository extends JpaRepository<OutputEntity, Integer> {
}
