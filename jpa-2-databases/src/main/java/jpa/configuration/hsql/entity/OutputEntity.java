package jpa.configuration.hsql.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Min;

@Entity
@Getter
@Setter
@ToString
public class OutputEntity {

    @Id
    @Min(0)
    private Integer id;

    private String firstName;
    private String lastName;
    private int salary;
    private int age;
    private int difference;

}
