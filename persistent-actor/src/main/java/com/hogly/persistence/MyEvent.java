package com.hogly.persistence;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MyEvent implements Serializable {

  @JsonProperty
  private Integer code;
  @JsonProperty
  private String name;

  @JsonCreator
  public MyEvent(Integer code, String name) {
    this.code = code;
    this.name = name;
  }

  public Integer code() {
    return code;
  }

  public String name() {
    return name;
  }

  @Override
  public String toString() {
    return "MyEvent{" +
      "code=" + code +
      ", name='" + name + '\'' +
      '}';
  }
}
