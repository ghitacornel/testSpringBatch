package jpa.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    private final JobLauncher jobLauncher;

    @GetMapping("jobJpaReadWriteErrorHandling")
    public void jobJpaReadWriteErrorHandling(@RequestParam(value = "count", defaultValue = "10000") long count) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addLong("count", count)
                    .toJobParameters();
            jobLauncher.run(jobJpaReadWriteErrorHandling, jobParameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("jobJpaReadWritePerformanceMultipleThreads")
    public void jobJpaReadWritePerformanceMultipleThreads(@RequestParam(value = "count", defaultValue = "10000") long count) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addLong("count", count)
                    .toJobParameters();
            jobLauncher.run(jobJpaReadWritePerformanceMultipleThreads, jobParameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("jobJpaReadWriteValidate")
    public void jobJpaReadWriteValidate(@RequestParam(value = "count", defaultValue = "10000") long count) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addLong("count", count)
                    .toJobParameters();
            jobLauncher.run(jobJpaReadWriteValidate, jobParameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("jobSingleThread")
    public void jobSingleThread(@RequestParam(value = "count", defaultValue = "10000") long count) {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addLong("count", count)
                    .toJobParameters();
            jobLauncher.run(jobSingleThread, jobParameters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
