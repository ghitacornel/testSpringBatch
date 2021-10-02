package main.jobs.jdbc.performance;

import com.github.javafaker.Faker;

public class InputDTO {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private long salary;

    private static final Faker faker = new Faker();

    public static InputDTO generate() {
        InputDTO inputDTO = new InputDTO();
        inputDTO.setId(faker.number().numberBetween(1, Integer.MAX_VALUE));
        inputDTO.setFirstName(faker.name().firstName());
        inputDTO.setLastName(faker.name().lastName());
        inputDTO.setAge(faker.number().numberBetween(1, 100));
        inputDTO.setSalary(faker.number().numberBetween(1000, 20000));
        return inputDTO;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public long getSalary() {
        return salary;
    }

    public void setSalary(long salary) {
        this.salary = salary;
    }

}