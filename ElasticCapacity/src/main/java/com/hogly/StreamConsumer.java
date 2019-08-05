package com.hogly;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.alpakka.elasticsearch.ElasticsearchSourceSettings;
import akka.stream.alpakka.elasticsearch.javadsl.ElasticsearchSource;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class StreamConsumer {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("stream-consumer");

    HttpHost httpHost = new HttpHost("localhost", 9200);
    RestClient restClient = RestClient.builder(httpHost).build();


    export(restClient, "integrationtests-payrecord", "payrecord", QueryBuilders.matchAllQuery(), "c:\\tmp\\payrecord.txt")


  }

  private static CompletionStage<Done> export(RestClient restClient, String esIndex, String esType, QueryBuilder queryBuilder, String fileName) {
    ElasticsearchSourceSettings sourceSettings = ElasticsearchSourceSettings.create()
      .withBufferSize(100)
      .withScrollDuration(FiniteDuration.apply(5, TimeUnit.MINUTES));

    ElasticsearchSource
      .create(esIndex, esType, queryBuilder.toString(), sourceSettings, restClient);

  }

}
