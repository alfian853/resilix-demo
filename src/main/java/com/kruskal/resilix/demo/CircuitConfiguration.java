package com.kruskal.resilix.demo;

import com.kruskal.resilix.springboot.v1.ResilixProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class CircuitConfiguration {


  @Bean
  public CircuitBreakerRegistry circuitBreakerRegistry(ResilixProperties resilixProperties){

    com.kruskal.resilix.core.Configuration configuration = resilixProperties.getConfig().get("foo");

    // Create a custom configuration for a CircuitBreaker
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        .failureRateThreshold(Math.round(configuration.getErrorThreshold()*100))
        .waitDurationInOpenState(Duration.ofMillis(configuration.getWaitDurationInOpenState()))
        .permittedNumberOfCallsInHalfOpenState(configuration.getNumberOfRetryInHalfOpenState())
        .minimumNumberOfCalls(configuration.getMinimumCallToEvaluate())
        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
        .slidingWindowSize((int) configuration.getSlidingWindowMaxSize())
        .build();

    // Create a CircuitBreakerRegistry with a custom global configuration
    CircuitBreakerRegistry circuitBreakerRegistry =
        CircuitBreakerRegistry.of(circuitBreakerConfig);

    return circuitBreakerRegistry;
  }

}
