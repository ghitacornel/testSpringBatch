package main.batch.steps.step4.processors;

import main.batch.steps.step4.model.Step4OutputDataModel;
import main.databases.postgresql.domain.PersonPostgreSQL;
import org.springframework.batch.item.ItemProcessor;

public class Step4ItemProcessor implements ItemProcessor<PersonPostgreSQL, Step4OutputDataModel> {

    @Override
    public Step4OutputDataModel process(PersonPostgreSQL inputDataModel) throws Exception {
        Step4OutputDataModel outputDataModel = new Step4OutputDataModel();
        outputDataModel.setId(inputDataModel.getId());
        outputDataModel.setName(inputDataModel.getName());
        outputDataModel.setSalary(inputDataModel.getSalary());
        outputDataModel.setAge(inputDataModel.getAge());
        return outputDataModel;
    }

}
