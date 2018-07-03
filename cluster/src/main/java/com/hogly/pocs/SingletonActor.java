package com.hogly.pocs;

import akka.Done;
import akka.actor.AbstractLoggingActor;

public class SingletonActor extends AbstractLoggingActor {

  @Override
  public void preStart() {
    log().info("preStart");
  }

  @Override
  public void postStop() {
    log().info("postStop");
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(Done.getInstance().getClass(), this::handleDone)
      .matchAny(msg -> log().warning("Unknown message: {} from: {}", msg, sender()))
      .build();
  }

  private void handleDone(Done done) {
    log().info("Handing done...");
  }
}
