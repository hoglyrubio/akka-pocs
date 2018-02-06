package com.hogly;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Sink;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class StreamingFromCassandra {

  public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {

    Session session = Cluster.builder()
      .addContactPoint("localhost")
      .withPort(9042)
      .build()
      .connect();

    ActorSystem system = ActorSystem.create();
    ActorMaterializer materializer = ActorMaterializer.create(system);

    Statement statement = new SimpleStatement("SELECT * FROM alpakka_tests.streaming")
      .setFetchSize(20);

    CompletionStage<List<Row>> rows = CassandraSource.create(statement, session)
      .map(row -> {
        List<ColumnDefinitions.Definition> columnDefinition = row.getColumnDefinitions().asList();
        System.out.println(columnDefinition);
        return row;
      })
      .runWith(Sink.seq(), materializer);

  }

}
