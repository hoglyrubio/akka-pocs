package com.hogly.pocs;

import java.io.Serializable;

public class AggregateMessage<T> implements Serializable {

  private final AggregateId aggregateId;
  private final T payload;

  public AggregateMessage(AggregateId aggregateId, T payload) {
    this.aggregateId = aggregateId;
    this.payload = payload;
  }

  public AggregateId getAggregateId() {
    return aggregateId;
  }

  public T getPayload() {
    return payload;
  }

  @Override
  public String toString() {
    return "AggregateMessage {" +
      "aggregateId=" + aggregateId.id() +
      ", payload=" + payload +
      '}';
  }
}
