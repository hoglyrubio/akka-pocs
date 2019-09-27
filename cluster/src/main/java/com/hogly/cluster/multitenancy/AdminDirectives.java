package com.hogly.cluster.multitenancy;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import com.typesafe.config.Config;

import java.util.concurrent.CompletionStage;

public class AdminDirectives extends AllDirectives {

  private final ActorSystem system;
  private final Http http;
  private final ConnectHttp connect;
  private final ActorMaterializer materializer;

  public AdminDirectives(ActorSystem system, Config config) {
    this.system = system;
    this.http = Http.get(system);
    this.materializer = ActorMaterializer.create(system);
    this.connect = ConnectHttp.toHost(config.getString("host"), config.getInt("port"));
  }

  public CompletionStage<ServerBinding> start() {
    Flow<HttpRequest, HttpResponse, NotUsed> flow = createRoute().flow(system, materializer);
    return http.bindAndHandle(flow, connect, materializer);
  }

  private Route createRoute() {
    return get(() -> path("admin", () -> complete("OK")));
  }

}
