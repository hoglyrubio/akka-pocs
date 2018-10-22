package com.hogly;

import akka.Done;
import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.Pair;
import akka.pattern.PatternsCS;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SimpleStreaming {

  private static final Random RANDOM = new Random();
  private static final int PARALLELISM = 1;

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    simpleStreaming();
  }

  private static void simpleStreaming() throws ExecutionException, InterruptedException {
    ActorSystem system = ActorSystem.create("simple-stream");

    Source<Double, NotUsed> source = Source.from(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));

    ActorRef aggregate = system.actorOf(Props.create(AggregateActor.class));

    Flow<Double, Pair<BigDecimal, Double>, NotUsed> currentValueFlow = Flow.<Double>create()
      .mapAsync(PARALLELISM, value -> {
        sleep();
        return PatternsCS.ask(aggregate, new AggregateActor.GetValue(), 10000)
          .thenApply(currentValue -> Pair.create((BigDecimal) currentValue, value));
      });

    Flow<Pair<BigDecimal, Double>, Pair<BigDecimal, Double>, NotUsed> anyCalcFlow = Flow.<Pair<BigDecimal, Double>>create()
      .mapAsync(PARALLELISM, pair -> {
        sleep();
        system.log().info("Estoy calculando con base en {}", pair.first());
        return CompletableFuture.completedFuture(pair);
      });

    Flow<Pair<BigDecimal, Double>, Done, NotUsed> addValueFlow = Flow.<Pair<BigDecimal, Double>>create()
      .mapAsync(PARALLELISM, pair -> {
        sleep();
        return PatternsCS.ask(aggregate, new BigDecimal(pair.second()), 10000)
          .thenApply(Done.class::cast);
      });

    source
      .log("step 1")
      .via(currentValueFlow)
      .log("step 2")
      .via(anyCalcFlow)
      .log("step 3")
      .via(addValueFlow)
      .log("end")
      .runWith(Sink.ignore(), ActorMaterializer.create(system))
      .thenCompose(done -> PatternsCS.ask(aggregate, new AggregateActor.GetValue(), 10000)
        .thenApply(BigDecimal.class::cast))
      .thenAccept(System.out::println);
  }

  private static void sleep() {
    try {
      Thread.sleep(RANDOM.nextInt(1000));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }


}
