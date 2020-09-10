package main.databases.mysql.domain;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "person_mysql")
@Data
public class PersonMySQL {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "salary")
    private double salary;

    @Column(name = "age")
    private int age;

}
