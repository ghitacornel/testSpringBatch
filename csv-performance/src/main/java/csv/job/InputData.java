package csv.job;

import com.github.javafaker.Faker;
import lombok.Data;

@Data
public class InputData {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private long salary;

    private static final Faker faker = new Faker();

    public static InputData generate() {
        InputData inputData = new InputData();
        inputData.setId(faker.number().numberBetween(1, Integer.MAX_VALUE));
        inputData.setFirstName(faker.name().firstName());
        inputData.setLastName(faker.name().lastName());
        inputData.setAge(faker.number().numberBetween(1, 100));
        inputData.setSalary(faker.number().numberBetween(1000, 20000));
        return inputData;
    }

    public String toCsv() {
        return String.join(",", id.toString(), firstName, lastName, String.valueOf(age), String.valueOf(salary)) + "\n";
    }

}