package com.hogly.streaming;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class GroupBy {

  public static Random random = new Random();
  public static ActorSystem system = ActorSystem.create();

  public static void main(String[] args) throws ExecutionException, InterruptedException {

    int numOfTypes = 1000;

    List<Record> records = randomRecords(numOfTypes, 5000000);

    system.log().info("records: {}", records.size());
    List<Aggregation> result = Source.from(records)
      .map(record -> new Aggregation().add(record))
      .groupBy(numOfTypes, aggregation -> aggregation.type)
      .reduce((a, b) -> a.add(b))
      //.async()
      .mergeSubstreams()
      .runWith(Sink.seq(), ActorMaterializer.create(system))
      .toCompletableFuture()
      .get();

    system.log().info("Types: {}", result.size());
  }

  private static List<Record> randomRecords(int numOfTypes, int totalRecords) {
    List<Record> records = new ArrayList<>();
    for (int i = 0; i < totalRecords; i++) {
      String id = UUID.randomUUID().toString();
      String type = "type-" + random.nextInt(numOfTypes);
      Double value = random.nextDouble();
      records.add(new Record(id, type, value));
    }
    return records.stream()
      .sorted(Comparator.comparing(Record::type))
      .collect(Collectors.toList());
  }

  public static class Record {
    String id;
    String type;
    Double value;

    public Record(String id, String type, Double value) {
      this.id = id;
      this.type = type;
      this.value = value;
    }

    public String type() {
      return type;
    }

    @Override
    public String toString() {
      return "{" +
        "id='" + id + '\'' +
        ", type='" + type + '\'' +
        ", value=" + value +
        '}';
    }
  }

  public static class Aggregation {
    String type;
    Double value;

    public Aggregation() {
      this.type = null;
      this.value = 0D;
    }

    public Aggregation add(Aggregation agg) {
      if (agg.type.equals(this.type)) {
        this.value = this.value + agg.value;
      } else if (this.type == null) {
        this.type = agg.type;
        this.value = agg.value;
      } else {
        throw new IllegalStateException("Received type: " + agg.type + " when the first type was " + this.type);
      }
      return this;
    }

    public Aggregation add(Record record) {
      if (record.type.equals(this.type)) {
        this.value = this.value + record.value;
      } else if (this.type == null) {
        this.type = record.type;
        this.value = record.value;
      } else {
        throw new IllegalStateException("Received type: " + record.type + " when the first type was " + this.type);
      }
      return this;
    }

    @Override
    public String toString() {
      return "Aggregation{" +
        "type='" + type + '\'' +
        ", value=" + value +
        '}';
    }
  }

}
