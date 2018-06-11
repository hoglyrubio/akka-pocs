package com.hogly.pocs;

import akka.actor.AbstractLoggingActor;
import akka.actor.Status;

import java.util.Optional;

public class ActorSupervised extends AbstractLoggingActor {

  private Integer internalState;

  public ActorSupervised() {
    internalState = 0;
    log().info("Constructor. internalState: {}", internalState);
  }

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

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(Messages.ExecuteSomething.class, this::handleExecuteSomething)
      .match(Messages.ExecuteWithStoppableException.class, this::handleExecuteWithStoppableException)
      .match(Messages.ExecuteWithResumableException.class, this::handleExecuteWithResumableException)
      .match(Messages.ExecuteWithRestartableException.class, this::handleExecuteWithRestartableException)
      .build();
  }

  private void handleExecuteWithStoppableException(Messages.ExecuteWithStoppableException msg) {
    throw new Exceptions.StopableException();
  }

  private void handleExecuteWithResumableException(Messages.ExecuteWithResumableException msg) {
    throw new Exceptions.ResumableException();
  }

  private void handleExecuteWithRestartableException(Messages.ExecuteWithRestartableException msg) {
    throw new Exceptions.RestartableException();
  }

  private void handleExecuteSomething(Messages.ExecuteSomething msg) {
    internalState++;
    log().info("Received. internalState: {}", internalState);
    sender().tell(new Status.Success(msg), self());
  }

}
