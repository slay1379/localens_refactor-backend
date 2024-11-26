package com.example.localens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.example.localens.analysis",
        "com.example.localens.influx",
        "com.example.localens.member"
})
public class LocalensApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocalensApplication.class, args);
    }

}
