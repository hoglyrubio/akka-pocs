package com.hogly.pocs;

import java.io.Serializable;
import java.util.UUID;

public class AggregateId implements Serializable {

  private final String id;

  AggregateId(String id) {
    this.id = id;
  }

  public static AggregateId create() {
    return new AggregateId(UUID.randomUUID().toString());
  }

  public String id() {
    return id;
  }
}
