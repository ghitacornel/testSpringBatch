package csv.job;

import lombok.Data;

@Data
public class OutputItem {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private long salary;
    private long difference;

}