package com.example.localens;

import com.example.localens.analysis.service.StatsBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@RequiredArgsConstructor
public class LocalensApplication implements CommandLineRunner {

    private final StatsBatchService statsBatchService;
    public static void main(String[] args) {
        SpringApplication.run(LocalensApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        statsBatchService.updateMinMaxStatistics();
    }

}