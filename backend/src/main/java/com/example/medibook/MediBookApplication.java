package com.example.medibook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableAsync
@SpringBootApplication
public class MediBookApplication {
  public static void main(String[] args) {
    SpringApplication.run(MediBookApplication.class, args);
  }
}
