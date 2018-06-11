package com.hogly.pocs;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Status;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ActorSupervisedTest {

  private static ActorSystem system;

  @BeforeClass
  public static void setUp() {
    system = ActorSystem.create();
  }

  @AfterClass
  public static void tearDown() {
    TestKit.shutdownActorSystem(system);
    system = null;
  }

  @Test
  public void shouldStopNormally() {
    new TestKit(system) {
      {
        Props props = Props.create(ActorSupervisor.class, ActorSupervisor::new);
        TestActorRef<ActorSupervisor> actor = TestActorRef.create(system, props);
        send(actor, Messages.ExecuteSomething.getInstance());
        expectMsgClass(Status.Success.class);
        send(actor, PoisonPill.getInstance());
        expectNoMsg();
      }
    };
  }

  @Test
  public void testResume() {
    new TestKit(system) {
      {
        Props props = Props.create(ActorSupervisor.class, ActorSupervisor::new);
        ActorRef supervisor = system.actorOf(props, "my-supervisor");
        send(supervisor, Messages.ExecuteWithResumableException.getInstance());
        expectNoMsg();
        send(supervisor, Messages.ExecuteWithResumableException.getInstance());
        expectNoMsg();
        send(supervisor, Messages.ExecuteWithResumableException.getInstance());
        expectNoMsg();
      }
    };
  }

  @Test
  public void testRestart() {
    new TestKit(system) {
      {
        Props props = Props.create(ActorSupervisor.class, ActorSupervisor::new);
        ActorRef supervisor = system.actorOf(props, "my-supervisor");
        send(supervisor, Messages.ExecuteSomething.getInstance());
        expectMsgClass(Status.Success.class);
        send(supervisor, Messages.ExecuteWithRestartableException.getInstance());
        expectNoMsg();
        send(supervisor, Messages.ExecuteWithRestartableException.getInstance());
        expectNoMsg();
        send(supervisor, Messages.ExecuteWithRestartableException.getInstance());
        expectNoMsg();
      }
    };
  }

}
