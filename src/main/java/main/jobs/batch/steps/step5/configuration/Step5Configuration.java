package main.jobs.batch.steps.step5.configuration;

import main.jobs.batch.listeners.CustomStepListener;
import main.jobs.batch.steps.step5.model.AllElements;
import main.jobs.batch.steps.step5.model.Step5OutputModel;
import main.jobs.batch.steps.step5.model.RowElement;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

@Configuration
public class Step5Configuration {

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Value("${step4.output.file}")
    String step5InputFile;

    @Value("${step5.output.file}")
    String step5OutputFile;

    @Bean
    public Step step5() throws Exception {
        return stepBuilderFactory.get("step5")
                .tasklet((contribution, chunkContext) -> {

                    // manual read
                    JAXBContext jaxbContext = JAXBContext.newInstance(AllElements.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    AllElements allElements = (AllElements) unmarshaller.unmarshal(new FileSystemResource(step5InputFile).getInputStream());

                    // manual process
                    Step5OutputModel result = new Step5OutputModel();
                    result.setCount(allElements.getItems().size());
                    for (RowElement item : allElements.getItems()) {
                        result.setAverageAge(result.getAverageAge() + item.getAge());
                        result.setSumAllSalaries(result.getSumAllSalaries() + item.getSalary());
                    }
                    if (result.getCount() > 0) {
                        result.setAverageAge(result.getAverageAge() / result.getCount());
                        result.setAverageSalary(result.getSumAllSalaries() / result.getCount());
                    }

                    // manual write
                    File file = new FileSystemResource(step5OutputFile).getFile();
                    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                    writer.write(String.valueOf(result));
                    writer.close();

                    // manual decide to continue or not
                    return RepeatStatus.FINISHED;

                })
                .listener(new CustomStepListener())
                .build();
    }

}
