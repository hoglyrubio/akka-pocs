package com.hogly.pocs;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.AllForOneStrategy;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import scala.PartialFunction;
import scala.concurrent.duration.Duration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class ActorSupervisor extends AbstractLoggingActor {

  private final ActorRef child;
  private Integer internalState;
  private static final PartialFunction<Throwable, SupervisorStrategy.Directive> decider = DeciderBuilder
    .match(Exceptions.StopableException.class, e -> {
      System.out.println("decider: STOP");
      return SupervisorStrategy.stop();
    })
    .match(Exceptions.RestartableException.class, e -> {
      System.out.println("decider: RESTART");
      return SupervisorStrategy.restart();
    })
    .match(Exceptions.ResumableException.class, e -> {
      System.out.println("decider: RESUME");
      return SupervisorStrategy.resume();
    })
    .matchAny(e -> {
      System.out.println("decider: ESCALATE");
      return SupervisorStrategy.escalate();
    })
    .build();
  private static OneForOneStrategy oneForOneStrategy = new OneForOneStrategy(10, Duration.create(1, TimeUnit.MINUTES), decider);
  private static AllForOneStrategy allForOneStrategy = new AllForOneStrategy(true, decider);

  @Override
  public void preStart() throws Exception {
    log().info("preStart. internalState:  {}", internalState);
    super.preStart();
  }

  @Override
  public void preRestart(Throwable reason, Optional<Object> message) throws Exception {
    log().warning("preRestart. Reason: {} internalState: {}", reason, internalState);
    super.preRestart(reason, message);
  }

  @Override
  public void postStop() throws Exception {
    log().info("postStop. internalState: {}", internalState);
    super.postStop();
  }

  @Override
  public void postRestart(Throwable reason) throws Exception {
    log().warning("postRestart. Reason: {}. internalState: {}", reason, internalState);
    super.postRestart(reason);
  }

  public ActorSupervisor() {
    Props props = Props.create(ActorSupervised.class, ActorSupervised::new);
    child = context().actorOf(props, "child");
    internalState = 0;
    log().info("Constructor. internalState: {}", internalState);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(Messages.ExecuteSomething.class, msg -> child.tell(msg, sender()))
      .match(Messages.ExecuteWithStoppableException.class, msg -> child.tell(msg, sender()))
      .match(Messages.ExecuteWithResumableException.class, msg -> child.tell(msg, sender()))
      .match(Messages.ExecuteWithRestartableException.class, msg -> child.tell(msg, sender()))
      .build();
  }

  @Override
  public SupervisorStrategy supervisorStrategy() {
    return oneForOneStrategy;
  }

}
