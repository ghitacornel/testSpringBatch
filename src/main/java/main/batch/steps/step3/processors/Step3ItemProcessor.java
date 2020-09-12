package main.batch.steps.step3.processors;

import main.databases.mysql.domain.PersonMySQL;
import main.databases.postgresql.domain.PersonPostgreSQL;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;

public class Step3ItemProcessor implements ItemProcessor<PersonMySQL, PersonPostgreSQL> {

    @Override
    public PersonPostgreSQL process(PersonMySQL inputDataModel) throws Exception {
        PersonPostgreSQL outputDataModel = new PersonPostgreSQL();
        outputDataModel.setId(inputDataModel.getId());
        outputDataModel.setName(inputDataModel.getName());
        outputDataModel.setSalary(BigDecimal.valueOf(inputDataModel.getSalary()));
        outputDataModel.setAge(inputDataModel.getAge());
        return outputDataModel;
    }

}
