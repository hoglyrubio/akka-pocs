import akka.actor.AbstractLoggingActor;

public class EntityActor extends AbstractLoggingActor {

  @Override
  public void preStart() {
    //log().info("Starting");
  }

  @Override
  public void postStop() {
    //log().info("Stopping");
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(EntityMessage.class, this::handleMessage)
      .build();
  }

  private void handleMessage(EntityMessage msg) {
    log().info("I´m handling: {} from: {}", msg, sender());
    sender().tell(EntityId.create(), self());
  }

}
