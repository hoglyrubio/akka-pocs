package com.hogly;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class StreamSinkSeqExceptions {

  public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {

    ActorSystem system = ActorSystem.create();

    CompletionStage<List<Integer>> response = Source.range(1, 10)
      .map(a -> validations(a))
      .runWith(Sink.seq(), ActorMaterializer.create(system));

    response
      .thenAccept(value -> {
        System.out.println("Finished: " + value);
      })
      .exceptionally(e -> {
        System.err.println(e);
        throw new IllegalArgumentException("Finished with errors");
      });
  }

  private static Integer validations(Integer value) {
    System.out.println("Validating: " + value);
    if (value % 2 == 0) {
      throw new IllegalArgumentException("A big problem");
    }
    return value;
  }

}
