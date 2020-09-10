package main.batch.steps.step4.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Data
@XmlRootElement(name = "person_row")
@XmlAccessorType(XmlAccessType.NONE)
public class Step4OutputDataModel {

    @XmlAttribute(name = "id")
    private Integer id;

    @XmlAttribute(name = "real-name")
    private String name;

    @XmlAttribute(name = "salary")
    private double salary;

    @XmlAttribute(name = "age")
    private int age;

}
