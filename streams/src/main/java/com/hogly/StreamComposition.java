package com.hogly;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class StreamComposition {

  static ActorSystem system = ActorSystem.create("testing");

  public static void main(String[] args) {
    StreamComposition app = new StreamComposition();
    app.sample0()
      .thenAccept(done -> System.out.println("Finished"))
      .exceptionally(e -> {
        system.log().error(e, "Failure");
        return null;
      });
  }

  private CompletionStage<Done> sample0 () {
    return Source.range(1, 1000)
      .groupBy(5, value -> value % 2 == 0)
      .log("1")
      .mergeSubstreams()
      .log("2")
      .runForeach(System.out::println, ActorMaterializer.create(system));
  }

  private void sample1() {
    CompletionStage<Done> result = Source.range(1, 1000)
      //.mapAsync(1, this::step1)
      .mapAsync(1, this::step2)
      .mapAsync(1, this::step3)
      .runWith(Sink.ignore(), ActorMaterializer.create(system));

    result
      .thenAccept(done -> System.out.println("Finished"))
      .exceptionally(e -> {
        system.log().error(e, "Failure");
        return null;
      });
  }

  public CompletionStage<Integer> step1(Integer value) {
    system.log().info("Step 1: {}", value);

    CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> value);
    CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> value);

    CompletableFuture<Integer> finalFuture = CompletableFuture.allOf(future1, future2)
      .thenCompose(aVoid -> {
        if (value % 2 != 0) {
          throw new RuntimeException("Any error");
        }
        return CompletableFuture.completedFuture(value);
      });

    return finalFuture;
  }

  private CompletionStage<Integer> step2(Integer value) {
    system.log().info("Step 2: {}", value);
    if (value % 2 != 0) {
      throw new RuntimeException("Any error");
    }
    return CompletableFuture.completedFuture(value);
  }

  private CompletionStage<Integer> step3(Integer value) {
    system.log().info("Step 3: {}", value);
    return CompletableFuture.completedFuture(value);
  }

}
