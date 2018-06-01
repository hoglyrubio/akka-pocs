package com.java.hogly.fsm;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class MyFsmTest {

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
        Props props = Props.create(MyFsm.class, () -> new MyFsm(getRef()));
        ActorRef myFsm = system.actorOf(props);
        send(myFsm, new MyFsmMessages.StartProcess());
        expectMsgClass(Status.Success.class);
      }
    };
  }

  @Test
  public void shouldFailBecauseMessageUnexpected() {
    new TestKit(system) {
      {
        Props props = Props.create(MyFsm.class, () -> new MyFsm(getRef()));
        ActorRef myFsm = system.actorOf(props);
        send(myFsm, new MyFsmMessages.Step1Finished());
        expectMsgClass(Status.Failure.class);
      }
    };
  }

}
