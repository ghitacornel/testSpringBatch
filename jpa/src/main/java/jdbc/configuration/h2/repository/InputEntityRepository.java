package jdbc.configuration.h2.repository;

import jdbc.configuration.h2.entity.InputEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InputEntityRepository extends JpaRepository<InputEntity, Integer> {
}
