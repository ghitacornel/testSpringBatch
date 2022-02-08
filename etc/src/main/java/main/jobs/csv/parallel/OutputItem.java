package main.jobs.csv.parallel;

public class OutputItem {

    private Integer id;
    private String firstName;
    private String lastName;
    private int age;
    private long salary;
    private String processingThread;

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

    public String getProcessingThread() {
        return processingThread;
    }

    public void setProcessingThread(String processingThread) {
        this.processingThread = processingThread;
    }
}