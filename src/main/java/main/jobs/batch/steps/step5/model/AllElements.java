package main.jobs.batch.steps.step5.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement(name = "root-tag-name")
@XmlAccessorType(XmlAccessType.FIELD)
public class AllElements {

    @XmlElement(name = "person_row")
    private List<RowElement> items;

}
