package com.kruskal.resilix.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class PgController {


  AtomicInteger fooCounter = new AtomicInteger(0);
  AtomicInteger barCounter = new AtomicInteger(0);

  @GetMapping("/foo")
  public String midtrans(){
    return "foo "+ fooCounter.incrementAndGet();
  }

  @GetMapping("/bar")
  public String bar(){
    barCounter.incrementAndGet();
    return "bar " + fooCounter.incrementAndGet();
  }


}
