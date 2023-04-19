package jdbc.configuration.h2.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Getter
@Setter
public class InputEntity {

    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    private int salary;
    private int age;

}
