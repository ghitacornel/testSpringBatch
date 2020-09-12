package main.batch.steps.step0.processors;

import com.github.javafaker.Faker;
import main.batch.steps.step0.model.DummyData;
import org.springframework.batch.item.ItemProcessor;

public class Step0ItemProcessor implements ItemProcessor<DummyData, DummyData> {

    @Override
    public DummyData process(DummyData item) throws Exception {
        item.setAge(new Faker().number().numberBetween(20, 50));
        item.setSalary(new Faker().number().randomDouble(2, 500, 5000));
        item.setName(new Faker().name().fullName());
        return item;
    }

}
