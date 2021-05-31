package com.kruskal.resilix.demo;

import com.kruskal.resilix.springboot.v2.EnableResilix;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableResilix
@SpringBootApplication
public class ResilixDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(ResilixDemoApplication.class, args);
  }

}
