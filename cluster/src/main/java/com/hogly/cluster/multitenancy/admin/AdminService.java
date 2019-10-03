package com.hogly.cluster.multitenancy.admin;

import akka.actor.ActorSystem;
import akka.actor.Terminated;
import com.hogly.cluster.multitenancy.client.ClientDescriptor;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class AdminService {

  private final Map<String, ClientDescriptor> currentClients;

  public AdminService(ActorSystem actorSystem) {
    this.currentClients = new ConcurrentHashMap<>();
  }

  public CompletionStage<ClientDescriptor> createClient(CreateClient command) {
    if (currentClients.containsKey(command.id())) {
      throw new IllegalArgumentException("Client " + command.id() + " already exists");
    }
    return ClientDescriptor.create(command)
      .thenApply(clientDescriptor -> currentClients.put(command.id(), clientDescriptor));
  }

  public CompletionStage<Terminated> deleteClient(String clientId) {
    if (currentClients.containsKey(clientId)) {
      return currentClients.get(clientId).terminate()
        .thenApply(terminated -> {
          currentClients.remove(clientId);
          return terminated;
        });
    }
    throw new IllegalArgumentException("Client " + clientId + " does not exist");
  }

  public CompletionStage<Collection<ClientDescriptor>> getClients() {
    return CompletableFuture.completedFuture(currentClients.values());
  }

  public CompletionStage<Object> getClient(String clientId) {
    if (currentClients.containsKey(clientId)) {
      return CompletableFuture.completedFuture(currentClients.get(clientId));
    }
    throw new IllegalArgumentException("Client " + clientId + " does not exist");
  }
}
