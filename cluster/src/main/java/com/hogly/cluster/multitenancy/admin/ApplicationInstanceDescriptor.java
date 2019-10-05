package com.hogly.cluster.multitenancy.admin;

import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.http.javadsl.ServerBinding;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hogly.cluster.multitenancy.ApplicationInstanceContext;
import com.hogly.cluster.multitenancy.client.AppController;
import com.hogly.cluster.multitenancy.client.AppService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import scala.compat.java8.FutureConverters;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class ApplicationInstanceDescriptor {

  @JsonProperty private final String clientId;
  @JsonProperty private final String name;
  @JsonProperty private final ServerBinding serverBinding;
  @JsonProperty private final InetSocketAddress inetSocketAddress;
  private final ApplicationInstanceContext applicationInstanceContext;

  private ApplicationInstanceDescriptor(String clientId, String name, ApplicationInstanceContext applicationInstanceContext) {
    this.clientId = clientId;
    this.name = name;
    this.actorSystem = applicationInstanceContext.get(ActorSystem.class);
    this.appService = appService;
    this.appController = appController;
    this.serverBinding = serverBinding;
    this.inetSocketAddress = serverBinding.localAddress();
  }

  public String clientId() {
    return clientId;
  }

  public String name() {
    return name;
  }

  public ActorSystem actorSystem() {
    return actorSystem;
  }

  public CompletionStage<Terminated> terminate() {
    actorSystem.log().info("Terminating client {} {}", clientId, name);
    return serverBinding.unbind()
      .thenCompose(unbound -> FutureConverters.toJava(actorSystem.terminate()));
  }
}
