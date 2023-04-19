package jdbc.job;

import com.github.javafaker.Faker;
import lombok.Data;

import java.util.concurrent.atomic.AtomicInteger;

@Data
public class InputDTO {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private long salary;

    private static final Faker faker = new Faker();
    private static final AtomicInteger ids = new AtomicInteger(0);

    public static InputDTO generate() {
        InputDTO inputDTO = new InputDTO();
        inputDTO.setId(ids.getAndIncrement());
        inputDTO.setFirstName(faker.name().firstName());
        inputDTO.setLastName(faker.name().lastName());
        inputDTO.setAge(faker.number().numberBetween(1, 100));
        inputDTO.setSalary(faker.number().numberBetween(1000, 20000));
        return inputDTO;
    }

}