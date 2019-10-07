package com.hogly.cluster.multitenancy.admin;

import akka.actor.Terminated;
import com.hogly.cluster.multitenancy.instance.ApplicationInstance;
import com.hogly.cluster.multitenancy.instance.ApplicationInstanceDescriptor;
import com.hogly.cluster.multitenancy.instance.CreateApplicationInstance;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AdminService {

  private final Map<String, ApplicationInstance> currentApplicationInstances;

  public AdminService() {
    this.currentApplicationInstances = new ConcurrentHashMap<>();
  }

  public CompletionStage<ApplicationInstance> createClient(CreateApplicationInstance command) {
    if (currentApplicationInstances.containsKey(command.id())) {
      throw new IllegalArgumentException("Client " + command.id() + " already exists");
    }
    return ApplicationInstance.create(command)
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

  public CompletableFuture<Set<ApplicationInstanceDescriptor>> getClients() {
    return CompletableFuture.completedFuture(currentApplicationInstances.values().stream().map(ApplicationInstance::getDescriptor).collect(Collectors.toSet()));
  }

  public CompletionStage<ApplicationInstanceDescriptor> getClient(String clientId) {
    if (currentApplicationInstances.containsKey(clientId)) {
      return CompletableFuture.completedFuture(currentApplicationInstances.get(clientId).getDescriptor());
    }
    throw new IllegalArgumentException("Client " + clientId + " does not exist");
  }
}
