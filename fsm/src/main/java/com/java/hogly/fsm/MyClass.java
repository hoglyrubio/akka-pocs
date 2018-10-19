package com.java.hogly.fsm;

import java.util.function.Supplier;

public class MyClass {

  private final Integer value;

  public MyClass(Integer value) {
    this.value = value;
  }

  public static Supplier<Integer> value() {
    return () -> this.value;
  }
}
