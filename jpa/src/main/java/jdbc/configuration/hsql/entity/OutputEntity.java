package jdbc.configuration.hsql.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
public class OutputEntity {

    @Id
    private Integer id;
    private String firstName;
    private String lastName;
    private int salary;
    private int age;
    private int difference;

}
