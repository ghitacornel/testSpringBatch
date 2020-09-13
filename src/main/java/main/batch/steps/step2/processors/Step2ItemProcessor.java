package main.batch.steps.step2.processors;

import main.batch.steps.step2.model.Step2InputDataModel;
import main.databases.mysql.domain.PersonMySQL;
import org.springframework.batch.item.ItemProcessor;

public class Step2ItemProcessor implements ItemProcessor<Step2InputDataModel, PersonMySQL> {

    @Override
    public PersonMySQL process(Step2InputDataModel inputDataModel) {
        PersonMySQL outputDataModel = new PersonMySQL();
        outputDataModel.setId(inputDataModel.getId());
        outputDataModel.setName(inputDataModel.getName());
        outputDataModel.setSalary(inputDataModel.getSalary());
        outputDataModel.setAge(inputDataModel.getAge());
        return outputDataModel;
    }

}
