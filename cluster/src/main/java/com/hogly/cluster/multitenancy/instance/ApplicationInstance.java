package com.hogly.cluster.multitenancy.instance;

import akka.actor.ActorSystem;
import akka.actor.Terminated;
import com.hogly.cluster.multitenancy.client.AppController;
import com.hogly.cluster.multitenancy.client.AppService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import scala.compat.java8.FutureConverters;

import java.util.concurrent.CompletionStage;

public class ApplicationInstance {

  private final ApplicationInstanceContext context;
  private final ApplicationInstanceDescriptor descriptor;

  private ApplicationInstance(ApplicationInstanceDescriptor descriptor, ApplicationInstanceContext context) {
    this.descriptor = descriptor;
    this.context = context;
  }

  public ApplicationInstanceContext getContext() {
    return context;
  }

  public ApplicationInstanceDescriptor getDescriptor() {
    return descriptor;
  }

  public static CompletionStage<ApplicationInstance> create(CreateApplicationInstance command) {
    // Configuration
    Config clientConfig = ConfigFactory.load("multitenancy-client.conf")
      .withValue("multitenancy.http.host", ConfigValueFactory.fromAnyRef("localhost"))
      .withValue("multitenancy.http.port", ConfigValueFactory.fromAnyRef(command.httpPort()));

    // The ActorSystem
    ActorSystem clientSystem = ActorSystem.create(command.id(), clientConfig);

    // Starting services
    AppService appService = new AppService(clientSystem);

    // Starting Controllers
    AppController appController = new AppController(clientSystem, clientConfig.getConfig("multitenancy.http"));

    // Setting up the ApplicationInstanceContext
    ApplicationInstanceContext context = ApplicationInstanceContext.newInstance()
      .add(Config.class, clientConfig)
      .add(ActorSystem.class, clientSystem)
      .add(AppService.class, appService);

    return appController.start()
      .thenApply(serverBinding -> {
        ApplicationInstanceDescriptor descriptor = new ApplicationInstanceDescriptor(command.id(), command.name(), serverBinding);
        return new ApplicationInstance(descriptor, context);
      });
  }

  public CompletionStage<Terminated> terminate() {
    ActorSystem system = context.get(ActorSystem.class);
    system.log().info("Terminating {}", descriptor);
    return descriptor.getServerBinding().unbind()
      .thenCompose(unbound -> FutureConverters.toJava(system.terminate()));
  }

}
