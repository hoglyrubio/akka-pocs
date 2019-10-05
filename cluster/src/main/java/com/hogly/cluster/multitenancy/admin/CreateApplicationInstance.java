package com.hogly.cluster.multitenancy.admin;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreateApplicationInstance {

  @JsonProperty
  private final String id;
  @JsonProperty
  private final String name;
  @JsonProperty
  private final Integer httpPort;
  @JsonProperty
  private final Integer akkaClusterPort;

  @JsonCreator
  public CreateApplicationInstance(@JsonProperty("id") String id, @JsonProperty("name") String name, @JsonProperty("httpPort") Integer httpPort, @JsonProperty("akkaClusterPort") Integer akkaClusterPort) {
    this.id = id;
    this.name = name;
    this.httpPort = httpPort;
    this.akkaClusterPort = akkaClusterPort;
  }

  public String id() {
    return id;
  }

  public String name() {
    return name;
  }

  public int httpPort() {
    return httpPort;
  }

  public int akkaClusterPort() {
    return akkaClusterPort;
  }
}
