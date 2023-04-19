package csv.job;

import com.github.javafaker.Faker;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InputGenerator {

    private static final Faker faker = new Faker();

    private static InputData generate() {
        InputData inputData = new InputData();
        inputData.setId(faker.number().numberBetween(1, Integer.MAX_VALUE));
        inputData.setFirstName(faker.name().firstName());
        inputData.setLastName(faker.name().lastName());
        inputData.setAge(faker.number().numberBetween(1, 100));
        inputData.setSalary(faker.number().numberBetween(1000, 20000));
        return inputData;
    }

    public static List<String> generate(long size) {
        return IntStream.iterate(0, i -> i < size, i -> i + 1)
                .mapToObj(i -> generate()).map(InputData::toCsv)
                .collect(Collectors.toList());
    }

}