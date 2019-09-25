package com.hogly;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Objects;

@JsonDeserialize(builder = MyPojoBuilder.class)
public class MyPojo {

  @JsonProperty private final Long longValue;
  @JsonProperty private final String stringValue;
  @JsonProperty private final Double doubleValue;
  @JsonProperty private final Integer integerValue;

  protected MyPojo(MyPojoBuilder builder) {
    this.longValue = builder.longValue;
    this.stringValue = builder.stringValue;
    this.doubleValue = builder.doubleValue;
    this.integerValue = builder.integerValue;
  }

  public Long getLongValue() {
    return longValue;
  }

  public String getStringValue() {
    return stringValue;
  }

  public Double getDoubleValue() {
    return doubleValue;
  }

  public Integer getIntegerValue() {
    return integerValue;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MyPojo that = (MyPojo) o;
    return Objects.equals(longValue, that.longValue) &&
      Objects.equals(stringValue, that.stringValue) &&
      Objects.equals(doubleValue, that.doubleValue) &&
      Objects.equals(integerValue, that.integerValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(longValue, stringValue, doubleValue, integerValue);
  }

}
