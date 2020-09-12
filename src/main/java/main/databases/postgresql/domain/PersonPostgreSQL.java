package main.databases.postgresql.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "person_postgresql", schema = "postgresql_database")
@Data
public class PersonPostgreSQL {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "salary")
    private BigDecimal salary;

    @Column(name = "age")
    private int age;

}
