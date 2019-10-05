package com.hogly.cluster.multitenancy.client;

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

import static akka.http.javadsl.server.PathMatchers.segment;

public class AppController extends AllDirectives {

  private final ActorSystem system;
  private final Http http;
  private final ConnectHttp connect;
  private final ActorMaterializer materializer;
  private final String host;
  private final int port;

  public AppController(ActorSystem system, Config httpConfig) {
    this.system = system;
    this.http = Http.get(system);
    this.materializer = ActorMaterializer.create(system);
    this.host = httpConfig.getString("host");
    this.port = httpConfig.getInt("port");
    this.connect = ConnectHttp.toHost(host, port);
  }

  public CompletionStage<ServerBinding> start() {
    Flow<HttpRequest, HttpResponse, NotUsed> flow = createRoute().flow(system, materializer);
    return http.bindAndHandle(flow, connect, materializer);
  }

  private Route createRoute() {
    return route(
      pathPrefix(segment("api").slash("client"),
        () -> route(
          get(() -> complete(system.name() + " on " + host + ":" + port)))
        )
    );
  }

}
