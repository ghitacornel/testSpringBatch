package jpa.configuration.h2.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
@ToString
public class InputEntity {

    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    private int salary;
    private int age;

}
