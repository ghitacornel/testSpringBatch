package main.batch.steps.step1.processors;

import main.batch.steps.step1.model.Step1InputDataModel;
import main.batch.steps.step1.model.Step1OutputDataModel;

import org.springframework.batch.item.ItemProcessor;

public class Step1ItemProcessor implements ItemProcessor<Step1InputDataModel, Step1OutputDataModel> {

    @Override
    public Step1OutputDataModel process(Step1InputDataModel inputDataModel) {
        Step1OutputDataModel outputDataModel = new Step1OutputDataModel();
        outputDataModel.setId(inputDataModel.getId());
        outputDataModel.setName(inputDataModel.getName());
        outputDataModel.setSalary(inputDataModel.getSalary());
        outputDataModel.setAge(inputDataModel.getAge());
        return outputDataModel;
    }

}
