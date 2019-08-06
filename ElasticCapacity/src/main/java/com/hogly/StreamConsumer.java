package com.hogly;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.IOResult;
import akka.stream.alpakka.elasticsearch.ElasticsearchSourceSettings;
import akka.stream.alpakka.elasticsearch.ReadResult;
import akka.stream.alpakka.elasticsearch.javadsl.ElasticsearchSource;
import akka.stream.javadsl.FileIO;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import scala.concurrent.duration.FiniteDuration;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class StreamConsumer {

  public static ObjectMapper mapper = new ObjectMapper();

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("stream-consumer");

    HttpHost httpHost = new HttpHost("localhost", 9200);
    RestClient restClient = RestClient.builder(httpHost).build();

    export(system, restClient, "integrationtests-payrecord", "payrecord", QueryBuilders.matchAllQuery(), "c:\\tmp\\payrecord.txt")
      .toCompletableFuture().thenAccept(System.out::println);
  }

  private static CompletionStage<IOResult> export(ActorSystem system, RestClient restClient, String esIndex, String esType, QueryBuilder queryBuilder, String fileName) {
    ElasticsearchSourceSettings sourceSettings = ElasticsearchSourceSettings.create()
      .withBufferSize(100)
      .withScrollDuration(FiniteDuration.apply(5, TimeUnit.MINUTES));

    Source<ReadResult<Map<String, Object>>, NotUsed> source = ElasticsearchSource
      .create(esIndex, esType, queryBuilder.toString(), sourceSettings, restClient);

    Sink<ByteString, CompletionStage<IOResult>> sinkFile = FileIO.toPath(Paths.get(fileName));

    return source
      .map(StreamConsumer::marshall)
      .map(ByteString::fromString)
      .runWith(sinkFile, ActorMaterializer.create(system));
  }

  private static String marshall(ReadResult<Map<String, Object>> readResult) {
    try {
      return mapper.writeValueAsString(readResult.source()) + '\n';
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error marshaling", e);
    }
  }

}
