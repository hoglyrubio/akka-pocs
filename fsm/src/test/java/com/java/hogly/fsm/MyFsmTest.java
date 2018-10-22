package com.java.hogly.fsm;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.pattern.Backoff;
import akka.pattern.BackoffSupervisor;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

public class MyFsmTest {

  private static ActorSystem system;

  @BeforeClass
  public static void beforeAll() {
    system = ActorSystem.create("fsm-testing");
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

        Props supervisorProps = BackoffSupervisor.props(Backoff.onFailure(props, "my-fsm",
          Duration.create(5, TimeUnit.SECONDS), Duration.create(15, TimeUnit.SECONDS), 0.2));

        ActorRef myFsm = system.actorOf(supervisorProps, "my-fsm-supervised");
        send(myFsm, new MyFsmMessages.StartProcess());
        expectMsgClass(Duration.apply(60, TimeUnit.SECONDS), Status.Success.class);
      }
    };
  }

  @Test
  public void shouldFailBecauseMessageUnexpected() {
    new TestKit(system) {
      {
        Props props = Props.create(MyFsm.class, () -> new MyFsm(getRef()));
        ActorRef myFsm = system.actorOf(props, "my-sick-fsm");
        send(myFsm, new MyFsmMessages.Step1Finished());
        expectMsgClass(Status.Failure.class);
      }
    };
  }

}
