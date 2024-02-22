package jpa.job;

import jpa.configuration.input.entity.InputEntity;
import jpa.configuration.output.entity.OutputEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessResult {

    private InputEntity input;
    private OutputEntity output;

}
