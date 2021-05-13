package com.kruskal.resilix.demo;

import com.kruskal.resilix.core.ResilixExecutor;
import com.kruskal.resilix.core.ResilixRegistry;
import com.kruskal.resilix.core.ResultWrapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class DemoController {


  @Autowired
  private ResilixRegistry resilixRegistry;

  private final String[] thirdPartyList = {"foo","bar"};


  @GetMapping("/demo")
  public String callApi() {

    for(String thirdParty: thirdPartyList) {

      ResilixExecutor resilixExecutor = resilixRegistry.getResilixExecutor(thirdParty);

      try {
        ResultWrapper<String> resultWrapper = resilixExecutor.execute(() -> this.proceed(thirdParty));
        if(resultWrapper.isExecuted()) return resultWrapper.getResult();
      } catch (Exception e) {
        e.printStackTrace();
        log.error("{} execution failed", thirdParty, e);
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
