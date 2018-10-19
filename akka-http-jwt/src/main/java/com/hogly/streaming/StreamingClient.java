package com.hogly.streaming;

import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.common.EntityStreamingSupport;
import akka.http.javadsl.common.JsonEntityStreamingSupport;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.concurrent.CompletionStage;

public class StreamingClient {

  private static ActorSystem system = ActorSystem.create();
  private static ActorMaterializer materializer = ActorMaterializer.create(system);
  private static Http http = Http.get(system);
  private static JsonEntityStreamingSupport jsonEntityStreamingSupport = EntityStreamingSupport.json();

  public static void main(String[] args) {
    HttpRequest request = HttpRequest
      .create("http://localhost:1977/random")
      .withMethod(HttpMethods.GET);

    /** Streaming */
    streamingClient(request, MyRecord.class)
      .thenCompose(source -> source.map(myRecord -> {
        system.log().info("Processing: {}", myRecord);
        return myRecord;
      }).runWith(Sink.ignore(), materializer))
      .exceptionally(e -> { system.log().error("Error", e); return null; });

    /** Classic */
    TypeReference<List<MyRecord>> typeReference = new TypeReference<List<MyRecord>>() {};
    classicClient(request)
      .thenApply(response -> MyMarshaller.fromJson(response, typeReference))
      .thenAccept(response -> system.log().info("RESPONSE: {}", response))
      .exceptionally(e -> { system.log().error("Error", e); return null; });
  }

  private static CompletionStage<String> classicClient(HttpRequest request) {
    return http.singleRequest(request, materializer)
      .thenCompose(httpResponse -> httpResponse.entity().getDataBytes()
        .runFold("", (current, byteString) -> current + byteString.decodeString("UTF-8"), materializer));
  }

  private static <T> CompletionStage<Source<MyRecord, Object>> streamingClient(HttpRequest request, Class<T> clazz) {
    return http.singleRequest(request, materializer)
      .thenApply(httpResponse -> httpResponse.entity().getDataBytes()
        .via(jsonEntityStreamingSupport.framingDecoder())
        .map(byteString -> byteString.utf8String())
        .map(json -> MyMarshaller.fromJson(json, MyRecord.class)));
  }

}
