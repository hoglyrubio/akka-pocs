package com.hogly.cluster.multitenancy.client;

import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.http.javadsl.ServerBinding;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hogly.cluster.multitenancy.admin.CreateClient;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import scala.compat.java8.FutureConverters;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletionStage;

public class ClientDescriptor {

  @JsonProperty private final String clientId;
  @JsonProperty private final String name;
  private final ActorSystem actorSystem;
  private final ClientService clientService;
  private final ClientController clientController;
  @JsonProperty private final ServerBinding serverBinding;
  @JsonProperty private final InetSocketAddress inetSocketAddress;

  private ClientDescriptor(String clientId, String name, ActorSystem actorSystem, ClientService clientService, ClientController clientController, ServerBinding serverBinding) {
    actorSystem.log().info("Starting client {} {}", clientId, name);
    this.clientId = clientId;
    this.name = name;
    this.actorSystem = actorSystem;
    this.clientService = clientService;
    this.clientController = clientController;
    this.serverBinding = serverBinding;
    this.inetSocketAddress = serverBinding.localAddress();
  }

  public static CompletionStage<ClientDescriptor> create(CreateClient command) {
    Config clientConfig = ConfigFactory.load("multitenancy-client.conf")
      .withValue("multitenancy.http.host", ConfigValueFactory.fromAnyRef("localhost"))
      .withValue("multitenancy.http.port", ConfigValueFactory.fromAnyRef(command.httpPort()));

    ActorSystem clientSystem = ActorSystem.create(command.id(), clientConfig);

    ClientService clientService = new ClientService(clientSystem);

    ClientController clientController = new ClientController(clientSystem, clientConfig.getConfig("multitenancy.http"));

    return clientController.start()
      .thenApply(serverBinding -> new ClientDescriptor(command.id(), command.name(), clientSystem, clientService, clientController, serverBinding));
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
