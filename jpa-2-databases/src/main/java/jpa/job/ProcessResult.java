package jpa.job;

import jpa.configuration.h2.entity.InputEntity;
import jpa.configuration.hsql.entity.OutputEntity;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProcessResult {

    private InputEntity input;
    private OutputEntity output;

}
