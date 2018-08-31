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
      .match(AggregateMessage.class, this::handleMessage)
      .build();
  }

  private void handleMessage(AggregateMessage msg) {
    log().info("I´m handling: {} from: {}", msg, sender());
    sender().tell(AggregateId.create(), self());
  }

}
