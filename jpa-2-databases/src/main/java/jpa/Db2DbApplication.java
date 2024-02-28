package jpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class Db2DbApplication {
    public static void main(String[] args) {
        SpringApplication.run(Db2DbApplication.class, args);
    }
}
