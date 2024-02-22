package jpa.configuration.output.repository;

import jpa.configuration.output.entity.OutputEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutputEntityRepository extends JpaRepository<OutputEntity, Integer> {
}
