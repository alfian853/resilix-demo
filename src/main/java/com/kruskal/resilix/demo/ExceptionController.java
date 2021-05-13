package com.kruskal.resilix.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {


  @ExceptionHandler({Exception.class})
  public ResponseEntity<String> handleError(Exception e){
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
        .body(e.getMessage());
  }

}
