package com.hogly.cluster.multitenancy.admin;

import akka.actor.ActorSystem;
import akka.actor.Terminated;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

public class AdminService {

  private final Map<String, ApplicationInstanceDescriptor> currentApplicationInstances;

  public AdminService(ActorSystem actorSystem) {
    this.currentApplicationInstances = new ConcurrentHashMap<>();
  }

  public CompletionStage<ApplicationInstanceDescriptor> createClient(CreateApplicationInstance command) {
    if (currentApplicationInstances.containsKey(command.id())) {
      throw new IllegalArgumentException("Client " + command.id() + " already exists");
    }
    return ApplicationInstanceDescriptor.create(command)
      .thenApply(applicationInstanceDescriptor -> currentApplicationInstances.put(command.id(), applicationInstanceDescriptor));
  }

  public CompletionStage<Terminated> deleteClient(String clientId) {
    if (currentApplicationInstances.containsKey(clientId)) {
      return currentApplicationInstances.get(clientId).terminate()
        .thenApply(terminated -> {
          currentApplicationInstances.remove(clientId);
          return terminated;
        });
    }
    throw new IllegalArgumentException("Client " + clientId + " does not exist");
  }

  public CompletionStage<Collection<ApplicationInstanceDescriptor>> getClients() {
    return CompletableFuture.completedFuture(currentApplicationInstances.values());
  }

  public CompletionStage<Object> getClient(String clientId) {
    if (currentApplicationInstances.containsKey(clientId)) {
      return CompletableFuture.completedFuture(currentApplicationInstances.get(clientId));
    }
    throw new IllegalArgumentException("Client " + clientId + " does not exist");
  }
}
