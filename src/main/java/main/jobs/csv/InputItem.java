package main.jobs.csv;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class InputItem {

    @NotNull
    private Integer id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Min(20)
    @Max(65)
    private int age;

    @Min(100)
    private long salary;

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

    public static void main(String[] args) {
        for (int i = 0; i < 1000; i++) {
            InputItem inputItem = new InputItem();
            inputItem.setId(i);
            inputItem.setFirstName("name" + i);
            inputItem.setLastName("surname" + i);
            inputItem.setAge(i % 45 + 20+1);
            inputItem.setSalary(i + 100+2);
            System.out.println(inputItem.id + "," + inputItem.getFirstName() + "," + inputItem.getLastName() + "," + inputItem.getAge() + "," + inputItem.getSalary()+","+(inputItem.getSalary()-inputItem.getAge()));
        }
    }
}