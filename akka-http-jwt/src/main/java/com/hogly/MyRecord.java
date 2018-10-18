package com.hogly;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MyRecord {

  @JsonProperty
  private final Integer id;
  @JsonProperty
  private final String value;

  @JsonCreator
  public MyRecord(@JsonProperty Integer id, @JsonProperty String value) {
    this.id = id;
    this.value = value;
  }

  public Integer getId() {
    return id;
  }

  public String getValue() {
    return value;
  }
}
