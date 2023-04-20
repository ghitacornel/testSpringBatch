package jdbc.job;

import lombok.Data;

@Data
public class InputDTO {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private long salary;

}