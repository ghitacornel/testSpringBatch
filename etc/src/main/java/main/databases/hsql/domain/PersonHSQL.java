package main.databases.hsql.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "person_hsql")
@Data
public class PersonHSQL {

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
