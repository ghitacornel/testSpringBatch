package jpa.configuration.hsql.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Min;

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
