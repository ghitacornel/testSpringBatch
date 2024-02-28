package jpa.services;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobsService {

    @Qualifier("jobJpaReadWriteErrorHandling")
    private final Job jobJpaReadWriteErrorHandling;

    @Qualifier("jobJpaReadWritePerformanceMultipleThreads")
    private final Job jobJpaReadWritePerformanceMultipleThreads;

    @Qualifier("jobJpaReadWriteValidate")
    private final Job jobJpaReadWriteValidate;

    @Qualifier("jobSingleThread")
    private final Job jobSingleThread;

    private final JobLauncher jobLauncher;

    @Async
    public void jpaReadWriteErrorHandling(long count) {
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

    @Async
    public void jpaReadWritePerformanceMultipleThreads(long count) {
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

    @Async
    public void jpaReadWriteValidate(long count) {
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

    @Async
    public void singleThread(long count) {
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
