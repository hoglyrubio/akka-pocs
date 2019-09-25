package com.hogly;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonPOJOBuilder(withPrefix = "set", buildMethodName = "build")
public class MyPojoBuilder {

  protected Long longValue;
  protected String stringValue;
  protected Double doubleValue;
  protected Integer integerValue;

  public MyPojoBuilder setLongValue(Long longValue) {
    this.longValue = longValue;
    return this;
  }

  public MyPojoBuilder setStringValue(String stringValue) {
    this.stringValue = stringValue;
    return this;
  }

  public MyPojoBuilder setDoubleValue(Double doubleValue) {
    this.doubleValue = doubleValue;
    return this;
  }

  public MyPojoBuilder setIntegerValue(Integer integerValue) {
    this.integerValue = integerValue;
    return this;
  }

  public MyPojo build() {
    return new MyPojo(this);
  }

}
