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
public class LocalensApplication {
    public static void main(String[] args) {
        SpringApplication.run(LocalensApplication.class, args);
    }
}