package jpa.configuration.h2.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
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

    @Enumerated(EnumType.STRING)
    private InputStatus status = InputStatus.NEW;

}
