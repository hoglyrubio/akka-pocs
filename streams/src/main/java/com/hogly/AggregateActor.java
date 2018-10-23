package com.hogly;

import akka.Done;
import akka.actor.AbstractLoggingActor;

import java.math.BigDecimal;

public class AggregateActor extends AbstractLoggingActor {

  private BigDecimal value = BigDecimal.ZERO;

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(BigDecimal.class, this::handleAdd)
      .match(GetValue.class, this::handleGet)
      .build();
  }

  private void handleGet(GetValue msg) {
    SleepUtils.sleep();
    sender().tell(this.value, self());
  }

  private void handleAdd(BigDecimal msg) {
    SleepUtils.sleep();
    this.value = this.value.add(msg);
    sender().tell(Done.getInstance(), self());
  }

  public static class GetValue {}

}
