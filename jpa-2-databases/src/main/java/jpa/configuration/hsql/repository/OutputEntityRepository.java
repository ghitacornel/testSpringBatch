package jpa.configuration.hsql.repository;

import jpa.configuration.hsql.entity.OutputEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutputEntityRepository extends JpaRepository<OutputEntity, Integer> {
}
