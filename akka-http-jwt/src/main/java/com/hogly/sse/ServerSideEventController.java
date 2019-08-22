package com.hogly.sse;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.marshalling.sse.EventStreamMarshalling;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.sse.ServerSentEvent;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.HeaderDirectives;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ServerSideEventController extends AllDirectives {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create();
    ServerSideEventController server = new ServerSideEventController(system, "localhost", 1977);
    server.start().exceptionally(t -> {
      system.log().error(t, "Error creating server");
      return null;
    });
  }

  private final ActorSystem system;
  private final Http http;
  private final ConnectHttp connect;
  private final ActorMaterializer materializer;
  private final Random random;

  public ServerSideEventController(ActorSystem system, String hostname, int port) {
    this.system = system;
    this.http = Http.get(system);
    this.materializer = ActorMaterializer.create(system);
    this.connect = ConnectHttp.toHost(hostname, port);
    this.random = new Random();
  }

  public CompletionStage<ServerBinding> start() {
    Flow<HttpRequest, HttpResponse, NotUsed> flow = createRoute().flow(system, materializer);
    return http.bindAndHandle(flow, connect, materializer);
  }

  private Route createRoute() {
    Iterable<HttpHeader> headers = Arrays.asList(
      HttpHeader.parse("Access-Control-Allow-Origin", "*"),
      HttpHeader.parse("Connection", "keep-alive"),
      HttpHeader.parse("Cache-Control", "no-cache")
    );
    return path("sse", () -> {
      Supplier<Route> sseEvents = () -> completeOK(randomNumbers(), EventStreamMarshalling.toEventStream());
      return respondWithHeaders(headers, sseEvents);
    });
  }

  private Source<ServerSentEvent, NotUsed> randomNumbers() {
    return Source.fromIterator(() -> Stream.generate(random::nextInt)
      .map(this::sleep)
      .map(value -> ServerSentEvent.create(UUID.randomUUID().toString(), "eventType", "eventId"))
      .iterator()
     );
  }

  private <T> T sleep(T value) {
    try {
      Thread.sleep(100);
      return value;
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
