package csv.job;

import lombok.Data;

@Data
public class InputData {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private long salary;

    public String toCsv() {
        return String.join(",", id.toString(), firstName, lastName, String.valueOf(age), String.valueOf(salary)) + "\n";
    }

}