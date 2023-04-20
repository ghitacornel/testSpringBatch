package jdbc.job;

import lombok.Data;

@Data
public class OutputDTO {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private long salary;
    private long difference;

}