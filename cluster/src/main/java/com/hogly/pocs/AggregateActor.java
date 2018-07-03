package com.hogly.pocs;

import akka.actor.AbstractLoggingActor;

public class AggregateActor extends AbstractLoggingActor {

  @Override
  public void preStart() throws Exception {
    log().info("Starting");
  }

  @Override
  public void postStop() throws Exception {
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
  }

}
