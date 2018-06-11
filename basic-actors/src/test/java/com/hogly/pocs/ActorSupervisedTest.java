package com.hogly.pocs;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.PoisonPill;
import akka.actor.Props;
import akka.actor.Status;
import akka.pattern.Backoff;
import akka.pattern.BackoffOptions;
import akka.pattern.BackoffSupervisor;
import akka.testkit.TestActorRef;
import akka.testkit.javadsl.TestKit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

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

  @Test
  public void testBackoffSupervision() {
    new TestKit(system) {
      {
        Props supervisorProps = BackoffSupervisor.props(Backoff.onStop(
          Props.create(ActorSupervised.class, ActorSupervised::new),
          "child",
          FiniteDuration.apply(3, TimeUnit.SECONDS),
          FiniteDuration.apply(30, TimeUnit.SECONDS),
          0.2
        ));
        ActorRef supervisor = system.actorOf(supervisorProps, "supervisor");
        send(supervisor, Messages.ExecuteWithStoppableException.getInstance());
        expectNoMsg(FiniteDuration.apply(30, TimeUnit.SECONDS));
      }
    };
  }

  @Test
  public void testDefaultBehaviour() {
    new TestKit(system) {
      {
        Props props = Props.create(ActorSupervised.class, ActorSupervised::new);
        ActorRef supervisor = system.actorOf(props);
        send(supervisor, Messages.ExecuteWithStoppableException.getInstance());
        expectNoMsg(FiniteDuration.apply(30, TimeUnit.SECONDS));
      }
    };
  }

}
