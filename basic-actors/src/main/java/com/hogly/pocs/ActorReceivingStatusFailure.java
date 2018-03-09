package com.hogly.pocs;

import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;

public class ActorReceivingStatusFailure extends AbstractLoggingActor {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create();
    ActorRef actor = system.actorOf(Props.create(ActorReceivingStatusFailure.class, () -> new ActorReceivingStatusFailure()));
    actor.tell(new Integer(123), actor);
    actor.tell(new Status.Failure(new RuntimeException("Hola!")), actor);
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(Integer.class, v -> log().info("Value: {}", v))
      .matchAny(msg -> log().info("Received unknown: {}", msg))
      .build();
  }

}
