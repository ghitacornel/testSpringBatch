package jdbc.configuration.hsql.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "person_hsql")
public class PersonHSQL {

    @Id
    private Integer id;
    private String name;
    private BigDecimal salary;
    private int age;

}
