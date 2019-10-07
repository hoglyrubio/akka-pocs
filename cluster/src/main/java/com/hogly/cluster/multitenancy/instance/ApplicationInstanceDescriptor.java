package com.hogly.cluster.multitenancy.instance;

import akka.http.javadsl.ServerBinding;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetSocketAddress;

public class ApplicationInstanceDescriptor {

  @JsonProperty private final String clientId;
  @JsonProperty private final String name;
  @JsonProperty private final InetSocketAddress inetSocketAddress;
  private final ServerBinding serverBinding;

  public ApplicationInstanceDescriptor(String clientId, String name, ServerBinding serverBinding) {
    this.clientId = clientId;
    this.name = name;
    this.serverBinding = serverBinding;
    this.inetSocketAddress = serverBinding.localAddress();
  }

  public String getClientId() {
    return clientId;
  }

  public String getName() {
    return name;
  }

  public InetSocketAddress getInetSocketAddress() {
    return inetSocketAddress;
  }

  public ServerBinding getServerBinding() {
    return serverBinding;
  }

  @Override
  public String toString() {
    return "ApplicationInstanceDescriptor{" +
      "clientId='" + clientId + '\'' +
      ", name='" + name + '\'' +
      ", inetSocketAddress=" + inetSocketAddress +
      '}';
  }
}
