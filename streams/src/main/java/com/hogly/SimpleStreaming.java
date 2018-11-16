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
import java.util.concurrent.CompletableFuture;

public class SimpleStreaming {

  private static final int PARALLELISM = 1;

  public static void main(String[] args) {
    simpleStreaming();
  }

  private static void simpleStreaming() {
    ActorSystem system = ActorSystem.create("simple-stream");

    //Source<Double, NotUsed> source = Source.from(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
    Source<Double, NotUsed> source = Source.repeat(new Double(1234.56));

    ActorRef aggregate = system.actorOf(Props.create(AggregateActor.class));

    source
      .map(Double::doubleValue)
      .mapAsync(PARALLELISM, v -> CompletableFuture.completedFuture(v))
      .via(flows(system, aggregate))
      .runWith(Sink.ignore(), ActorMaterializer.create(system))
      .thenCompose(done -> PatternsCS.ask(aggregate, new AggregateActor.GetValue(), 10000))
      .thenAccept(System.out::println);
  }

  public static Flow<Double, Done, NotUsed> flows(ActorSystem system, ActorRef aggregate) {
    Flow<Double, Pair<BigDecimal, Double>, NotUsed> stage1 = Flow.<Double>create()
      .mapAsync(PARALLELISM, value -> {
        return PatternsCS.ask(aggregate, new AggregateActor.GetValue(), 10000)
          .thenApply(currentValue -> Pair.create((BigDecimal) currentValue, value));
      });

    Flow<Pair<BigDecimal, Double>, Pair<BigDecimal, Double>, NotUsed> stage2 = Flow.<Pair<BigDecimal, Double>>create()
      .mapAsync(PARALLELISM, pair -> {
        return CompletableFuture.supplyAsync(() -> {
          SleepUtils.sleep(10);
          return pair;
        }, system.dispatcher());
      });

    Flow<Pair<BigDecimal, Double>, Pair<BigDecimal, Double>, NotUsed> stage3 = Flow.<Pair<BigDecimal, Double>>create()
      .mapAsync(PARALLELISM, pair -> {
        return CompletableFuture.supplyAsync(() -> {
          SleepUtils.sleep(1000);
          return pair;
        }, system.dispatcher());
      });

    Flow<Pair<BigDecimal, Double>, Done, NotUsed> stage4 = Flow.<Pair<BigDecimal, Double>>create()
      .mapAsync(PARALLELISM, pair -> {
        return PatternsCS.ask(aggregate, new BigDecimal(pair.second()), 10000)
          .thenApply(Done.class::cast);
      });

    return Flow.<Double>create()
      .log("stage1")
      .via(stage1)
      .log("stage2")
      .via(stage2)
      .log("stage3")
      .via(stage3)
      .log("stage4")
      .via(stage4)
      .log("end");
  }

}
