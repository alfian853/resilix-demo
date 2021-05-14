package com.kruskal.resilix.demo;

import com.kruskal.resilix.core.ResilixExecutor;
import com.kruskal.resilix.core.ResilixRegistry;
import com.kruskal.resilix.core.ResultWrapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class DemoController {


  @Autowired
  private ResilixRegistry resilixRegistry;

  @Autowired
  private CircuitBreakerRegistry circuitBreakerRegistry;

  private final String[] thirdPartyList = {"foo","bar"};


  @GetMapping("/resilix")
  public String callApi() {

    for(String thirdParty: thirdPartyList) {

      ResilixExecutor resilixExecutor = resilixRegistry.getResilixExecutor(thirdParty);

      try {
        ResultWrapper<String> resultWrapper = resilixExecutor.executeChecked(() -> this.proceed(thirdParty));

        //will skip if not execution isn't permitted
        if(resultWrapper.isExecuted()) return resultWrapper.getResult();
      }
      catch (SocketTimeoutException exception){
        //will throw error if failed and if retry failed
        throw new RuntimeException(thirdParty +" is still down");
      }
      catch (Throwable e) {
        //continue to the next if there is any unknown error;
        log.error("{} execution failed", thirdParty, e);
      }
    }

    log.error("all third parties are down");
    throw new RuntimeException("all third parties are down");
  }

  @GetMapping("/resilience4j")
  public String callApi2() {

    for(String thirdParty: thirdPartyList) {

      CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(thirdParty);

      try {
        return circuitBreaker.executeCheckedSupplier(() -> this.proceed(thirdParty));
      }
      catch (CallNotPermittedException exception){
        //continue to the next if not permitted;
        log.info("{} execution is not permitted yet", thirdParty, exception);
      }
      catch (SocketTimeoutException exception){
        //will throw error if failed and if retry failed
        throw new RuntimeException(thirdParty +" is still down");
      }
      catch (Throwable throwable) {
        //continue to the next if there is any unknown error;
        log.error("{} execution failed with throwable", thirdParty, throwable);
      }
    }

    log.error("all third parties are down");
    throw new RuntimeException("all third parties are down");
  }

  private String proceed(String thirdParty) throws IOException {

    String url = "";

    switch (thirdParty){
      case "foo":
        url = "http://localhost:3000/foo";
        break;
      case "bar":
        url = "http://localhost:5000/bar";
        break;
    }


    Request request = new Request.Builder()
        .get()
        .url(url)
        .build();


    Response response = new OkHttpClient.Builder()
        .connectTimeout(200, TimeUnit.MILLISECONDS)
        .readTimeout(200, TimeUnit.MILLISECONDS)
        .connectionPool(new ConnectionPool(200, 10, TimeUnit.MILLISECONDS))
        .build()
        .newCall(request).execute();
    return response.body().string();
  }

}
