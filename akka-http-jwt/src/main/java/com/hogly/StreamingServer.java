package com.hogly;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpCharsets;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.MediaType;
import akka.http.javadsl.model.MediaTypes;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;

import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.stream.Stream;

public class StreamingServer extends AllDirectives {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create();
    StreamingServer server = new StreamingServer(system, "localhost", 1977);
    server.start().exceptionally(t -> {
      system.log().error(t, "Error creating server");
      return null;
    });
  }

  private final ActorSystem system;
  private final Http http;
  private final ConnectHttp connect;
  private final ActorMaterializer materializer;

  private Random random;
  private MediaType.WithFixedCharset mediaType;
  private ContentType.WithFixedCharset contentType;
  private Marshaller<Compensation, RequestEntity> compensationMarshaller;
  private EntityStreamingSupport entityStreamingSupport;

  public StreamingServer(ActorSystem system, String hostname, int port) {
    this.system = system;
    this.http = Http.get(system);
    this.materializer = ActorMaterializer.create(system);
    this.connect = ConnectHttp.toHost(hostname, port);

    this.random = new Random();
    this.mediaType = MediaTypes.applicationWithFixedCharset("vnd.example.api.v1+json", HttpCharsets.UTF_8);
    //this.contentType = ContentTypes.create(mediaType);
    this.contentType = ContentTypes.APPLICATION_JSON;
    this.entityStreamingSupport = EntityStreamingSupport.json().withContentType(contentType).withParallelMarshalling(10, false);
    this.compensationMarshaller = Marshaller.withFixedContentType(contentType, (Compensation compensation) -> HttpEntities.create(contentType, compensation.toJson()));
  }

  public CompletionStage<ServerBinding> start() {
    Flow<HttpRequest, HttpResponse, NotUsed> flow = createRoute().flow(system, materializer);
    return Http.get(system).bindAndHandle(flow, connect, materializer);
  }

  private Route createRoute() {
    return route(
      get(() -> path("random", () -> randomNumbers()))
    );
  }

  private Route randomNumbers() {
    Source<Compensation, NotUsed> source = Source.fromIterator(() -> Stream.generate(random::nextInt)
      .map(this::sleep)
      .map(Compensation::new)
      .limit(100)
      .iterator());
    return completeOKWithSource(source, compensationMarshaller, entityStreamingSupport);
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
