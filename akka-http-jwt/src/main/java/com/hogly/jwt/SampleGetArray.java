package com.hogly.jwt;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class SampleGetArray extends AllDirectives {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("akka-http-get");
    SampleGetArray app = new SampleGetArray(system);
  }

  public SampleGetArray(ActorSystem system) {
    system.log().info("Starting application");
    Http http = Http.get(system);
    ActorMaterializer materializer = ActorMaterializer.create(system);
    Flow<HttpRequest, HttpResponse, NotUsed> flow = myRoutes().flow(system, materializer);
    ConnectHttp connectHttp = ConnectHttp.toHost("localhost", 8080);
    http.bindAndHandle(flow, connectHttp, materializer);
  }

  public Route myRoutes() {
    return route(
      get(() -> path("entities", () -> parameter("ids", ids -> queryManyIds(ids))))
    );
  }

  private Route queryManyIds(String ids) {
    List<String> body = Arrays.asList(ids.split(","));
    return complete(body.toString());
  }

}
