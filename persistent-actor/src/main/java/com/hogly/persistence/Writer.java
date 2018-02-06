package com.hogly.persistence;

import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.persistence.AbstractPersistentActor;

public class Writer extends AbstractPersistentActor {

  private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

  public Receive createReceiveRecover() {
    return receiveBuilder()
      .build();
  }

  public Receive createReceive() {
    return receiveBuilder()
      .match(MyEvent.class, this::handleEvent)
      .build();
  }

  public String persistenceId() {
    return "persistence-id";
  }

  private void handleEvent(MyEvent event) {
    persist(event, myEvent -> {
      log.info("Persisted {}", event);
    });
  }

}
