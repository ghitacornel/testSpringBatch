package jpa.configuration.input.repository;

import jpa.configuration.input.entity.InputEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InputEntityRepository extends JpaRepository<InputEntity, Integer> {
}
