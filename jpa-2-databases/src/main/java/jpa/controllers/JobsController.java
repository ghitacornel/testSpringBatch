package jpa.controllers;

import jpa.services.JobsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("jobs")
@RequiredArgsConstructor
public class JobsController {

    private final JobsService jobsService;

    @GetMapping("jpaReadWriteErrorHandling")
    public void jpaReadWriteErrorHandling(@RequestParam(value = "count", defaultValue = "10000") long count) {
        jobsService.jpaReadWriteErrorHandling(count);
    }

    @GetMapping("jpaReadWritePerformanceMultipleThreads")
    public void jpaReadWritePerformanceMultipleThreads(@RequestParam(value = "count", defaultValue = "10000") long count) {
        jobsService.jpaReadWritePerformanceMultipleThreads(count);
    }

    @GetMapping("jpaReadWriteValidate")
    public void jpaReadWriteValidate(@RequestParam(value = "count", defaultValue = "10000") long count) {
        jobsService.jpaReadWriteValidate(count);
    }

    @GetMapping("singleThread")
    public void singleThread(@RequestParam(value = "count", defaultValue = "10") long count) {
        jobsService.singleThread(count);
    }

}
