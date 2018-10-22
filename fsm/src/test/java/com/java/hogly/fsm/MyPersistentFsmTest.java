package com.java.hogly.fsm;

import akka.Done;
import akka.NotUsed;
import akka.actor.*;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.testkit.javadsl.TestKit;
import scala.concurrent.duration.FiniteDuration;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MyPersistentFsmTest {

  public static final FiniteDuration TIMEOUT = FiniteDuration.apply(10, TimeUnit.SECONDS);
  private static ActorSystem system;

  @BeforeClass
  public static void beforeAll() {
    system = ActorSystem.create();
  }

  @AfterClass
  public static void afterClass() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void shouldProcessAllSteps() {
    new TestKit(system) {
      {
        String id = UUID.randomUUID().toString();

        Props props = Props.create(MyPersistentFsm.class, () -> new MyPersistentFsm(id, getRef()));
        ActorRef myFsm = system.actorOf(props);

        send(myFsm, new MyFsmMessages.StartProcess());
        expectMsgClass(TIMEOUT, MyFsmMessages.StartStep1.class);
        send(myFsm, new MyFsmMessages.Step1Finished());
        expectMsgClass(TIMEOUT, MyFsmMessages.StartStep2.class);
        send(myFsm, new MyFsmMessages.Step2Finished());
        expectMsgClass(TIMEOUT, Status.Success.class);

        system.stop(myFsm);
        myFsm = system.actorOf(props);

        send(myFsm, new MyFsmMessages.StartProcess());
        expectMsgClass(TIMEOUT, Status.Success.class);
      }
    };
  }

  @Test(expected = InvalidActorNameException.class)
  public void testingIfActorExists() {
    new TestKit(system) {
      {
        Props props = Props.create(MyFsm.class, () -> new MyFsm(getRef()));
        system.actorOf(props, "my-actor-id");
        system.actorOf(props, "my-actor-id");
      }
    };
  }



  @Test
  public void testingSearchingActor() throws ExecutionException, InterruptedException {
    new TestKit(system) {
      {
        Props props = Props.create(MyFsm.class, () -> new MyFsm(getRef()));
        system.actorOf(props, "my-actor-id");
//        ActorSelection.
//        system.actorOf(props, "my-actor-id");


        Source<Integer, NotUsed> source = Source.from(Arrays.asList(1, 2, 3, 4));

        CompletionStage<Done> result = source
                .mapAsync(1, value -> {
                  return CompletableFuture.completedFuture(value * -1);
                })
                .map(value -> {
                  System.out.println(value);
                  return value;
                })
                .runWith(Sink.ignore(), ActorMaterializer.create(system));

        result.toCompletableFuture().get();
      }
    };
  }


}
