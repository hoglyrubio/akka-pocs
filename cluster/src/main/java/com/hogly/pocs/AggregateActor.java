package com.hogly.pocs;

import akka.Done;
import akka.actor.AbstractLoggingActor;

public class AggregateActor extends AbstractLoggingActor {

  @Override
  public void preStart() {
    log().info("Starting");
  }

  @Override
  public void postStop() {
    log().info("Stopping");
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .matchAny(this::handleMessage)
      .build();
  }

  private void handleMessage(Object msg) {
    log().info("I´m handling: {} from: {}", msg, sender());
    sender().tell(Done.getInstance(), self());
  }

}
