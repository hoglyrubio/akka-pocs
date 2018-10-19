package com.hogly.streaming;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MyRecord {

  @JsonProperty
  private final Integer id;
  @JsonProperty
  private final String value;

  @JsonCreator
  public MyRecord(@JsonProperty("id") Integer id, @JsonProperty("value") String value) {
    this.id = id;
    this.value = value;
  }

  public Integer getId() {
    return id;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "MyRecord{" +
      "id=" + id +
      ", value='" + value + '\'' +
      '}';
  }
}
