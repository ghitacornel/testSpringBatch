package jdbc.configuration.h2.repository;

import jdbc.configuration.h2.entity.InputEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonH2Repository extends JpaRepository<InputEntity, Integer> {
}
