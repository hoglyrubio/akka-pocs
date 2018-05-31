package com.java.hogly.fsm;

public class MyFsmData {

  private final String message;

  public MyFsmData(String message) {
    this.message = message;
  }

  public String message() {
    return message;
  }

  @Override
  public String toString() {
    return "{ message='" + message + '\'' +'}';
  }
}
