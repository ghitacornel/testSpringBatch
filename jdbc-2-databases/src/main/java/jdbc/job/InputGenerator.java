package jdbc.job;

import com.github.javafaker.Faker;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class InputGenerator {

    private static final Faker faker = new Faker();
    private static final AtomicInteger ids = new AtomicInteger(0);

    private static InputDTO generate() {
        InputDTO inputDTO = new InputDTO();
        inputDTO.setId(ids.getAndIncrement());
        inputDTO.setFirstName(faker.name().firstName());
        inputDTO.setLastName(faker.name().lastName());
        inputDTO.setAge(faker.number().numberBetween(1, 100));
        inputDTO.setSalary(faker.number().numberBetween(1000, 20000));
        return inputDTO;
    }

    public static List<InputDTO> generate(long size) {
        return IntStream.iterate(0, i -> i < size, i -> i + 1)
                .mapToObj(i -> InputGenerator.generate())
                .collect(Collectors.toList());
    }

}