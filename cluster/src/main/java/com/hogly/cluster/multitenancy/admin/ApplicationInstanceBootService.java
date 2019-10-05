package com.hogly.cluster.multitenancy.admin;

import akka.actor.ActorSystem;
import com.hogly.cluster.multitenancy.ApplicationInstanceContext;
import com.hogly.cluster.multitenancy.client.AppController;
import com.hogly.cluster.multitenancy.client.AppService;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;

import java.util.concurrent.CompletionStage;

public class ApplicationInstanceBootService {

  public static CompletionStage<ApplicationInstanceDescriptor> run(CreateApplicationInstance command) {
    Config clientConfig = buildConfig(command);
    ActorSystem clientSystem = ActorSystem.create(command.id(), clientConfig);

    // Starting services
    AppService appService = new AppService(clientSystem);

    // Starting Controllers
    AppController appController = new AppController(clientSystem, clientConfig.getConfig("multitenancy.http"));

    // Setting up the ApplicationInstanceContext
    ApplicationInstanceContext applicationInstanceContext = ApplicationInstanceContext.newInstance()
      .add(Config.class, clientConfig)
      .add(ActorSystem.class, clientSystem)
      .add(AppService.class, appService);

    return appController.start()
      .thenApply(serverBinding -> new ApplicationInstanceDescriptor(command.id(), command.name(), clientSystem, appService, appController, serverBinding));
  }

  private static Config buildConfig(CreateApplicationInstance command) {
    return ConfigFactory.load("multitenancy-client.conf")
      .withValue("multitenancy.http.host", ConfigValueFactory.fromAnyRef("localhost"))
      .withValue("multitenancy.http.port", ConfigValueFactory.fromAnyRef(command.httpPort()));
  }

}
