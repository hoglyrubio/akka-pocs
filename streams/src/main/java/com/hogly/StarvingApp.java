package com.hogly;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

import java.util.concurrent.CompletableFuture;

public class StarvingApp {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("starvaition-test");

    for (int i = 0; i < 100; i++) {
      final int x = i;
      System.out.println(x);
      CompletableFuture.supplyAsync(() -> {
        system.log().info("Starting: {}", x);
        SleepUtils.sleep();
        return x;
      });
    }

    /*Source.range(1, 1000)
      .mapAsync(1000, i -> {
        return CompletableFuture.supplyAsync(() -> {
          system.log().info("Starting: {}", i);
          SleepUtils.sleep(3000);
          return i;
        });
      })
      .map(i -> {
        system.log().info("Finished: {}", i);
        return i;
      })
      .runWith(Sink.seq(), ActorMaterializer.create(system))
      .thenAccept(values -> {
        system.log().info("{}", values);
      })
      .exceptionally(t -> {
        throw new RuntimeException("A problem", t);
      });*/

  }

}
