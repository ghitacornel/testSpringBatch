package jpa.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("jobs")
@RequiredArgsConstructor
public class JobsController {

    @Qualifier("jobJpaReadWriteErrorHandling")
    private final Job jobJpaReadWriteErrorHandling;

    @Qualifier("jobJpaReadWritePerformanceMultipleThreads")
    private final Job jobJpaReadWritePerformanceMultipleThreads;

    @Qualifier("jobJpaReadWriteValidate")
    private final Job jobJpaReadWriteValidate;

    @Qualifier("jobSingleThread")
    private final Job jobSingleThread;

    private final JobOperator jobOperator;

    @GetMapping("jobJpaReadWriteErrorHandling")
    public void jobJpaReadWriteErrorHandling() {
        try {
            jobOperator.startNextInstance(jobJpaReadWriteErrorHandling.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("jobJpaReadWritePerformanceMultipleThreads")
    public void jobJpaReadWritePerformanceMultipleThreads() {
        try {
            jobOperator.startNextInstance(jobJpaReadWritePerformanceMultipleThreads.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("jobJpaReadWriteValidate")
    public void jobJpaReadWriteValidate() {
        try {
            jobOperator.startNextInstance(jobJpaReadWriteValidate.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("jobSingleThread")
    public void jobSingleThread() {
        try {
            jobOperator.startNextInstance(jobSingleThread.getName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
