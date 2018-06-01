package com.java.hogly.fsm;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.javadsl.TestKit;

import java.util.UUID;

public class MyPersistentFsmTest {

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
        String id = "12345"; //UUID.randomUUID().toString();
        Props props = Props.create(MyPersistentFsm.class, () -> new MyPersistentFsm(id, getRef()));
        ActorRef myFsm = system.actorOf(props);
        send(myFsm, new MyFsmMessages.StartProcess());
        expectMsgClass(Status.Success.class);
      }
    };
  }

}
