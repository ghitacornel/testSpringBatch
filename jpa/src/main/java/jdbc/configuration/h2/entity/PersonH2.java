package jdbc.configuration.h2.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "person_h2")
@Getter
@Setter
public class PersonH2 {

    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    private double salary;
    private int age;

}
