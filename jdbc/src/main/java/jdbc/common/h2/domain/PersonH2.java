package jdbc.common.h2.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "person_h2")
public class PersonH2 {

    @Id
    private Integer id;
    private String name;
    private double salary;
    private int age;

}
