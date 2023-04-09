package org.brapi.brava.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("org.brapi.brava")
public class APIApplication {

  public static void main(String[] args) {
    SpringApplication.run(APIApplication.class, args);
  }

}