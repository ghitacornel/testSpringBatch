package main.databases.h2.repositories;

import main.databases.h2.domain.PersonH2;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonH2Repository extends JpaRepository<PersonH2, Integer> {
}
