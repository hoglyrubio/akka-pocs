package com.java.hogly.fsm;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.pattern.PatternsCS;
import akka.persistence.fsm.AbstractPersistentFSM;

import java.util.concurrent.CompletionStage;

public class MyPersistentFsm extends AbstractPersistentFSM<MyFsmState, MyFsmData, MyFsmMessages> {

  private String persistenceId;
  private ActorRef worker;

  public MyPersistentFsm(String persistenceId, ActorRef worker) {
    this.persistenceId = persistenceId;
    this.worker = worker;
  }

  {
    startWith(MyFsmState.IDLE, new MyFsmData("I'm idle"));

    when(MyFsmState.IDLE,
            matchEvent(MyFsmMessages.StartProcess.class, (event, data) -> goTo(MyFsmState.STEP_1)
                    .applying(new MyFsmMessages.Step1Started())
                    .andThen(exec(currentData -> {
                      log().info("andThen: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
                      worker.tell(new MyFsmMessages.StartStep1(), self());
                    }))
            )
            /*.anyEvent((event, data) -> {
              log().info("Unknown event: {} {}", event, data);
              return stay();
            })*/
    );

    when(MyFsmState.STEP_1,
            matchEvent(MyFsmMessages.Step1Finished.class, (event, data) -> goTo(MyFsmState.STEP_2)
                    .applying(new MyFsmMessages.Step2Started())
                    .andThen(exec(currentData -> {
                      log().info("andThen: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
                      //worker.tell(new MyFsmMessages.StartStep2(), self());
                      CompletionStage<Object> request = PatternsCS.ask(worker, new MyFsmMessages.StartStep2(), 1000);
                      PatternsCS.pipe(request, getContext().dispatcher()).to(self());
                    }))
            )
            /*.anyEvent((event, data) -> {
              log().info("Unknown event: {} {}", event, data);
              return stay();
            })*/
    );

    when(MyFsmState.STEP_2,
            matchEvent(MyFsmMessages.Step2Finished.class, (event, data) -> goTo(MyFsmState.DONE)
                    .applying(new MyFsmMessages.ProcessFinished())
                    .andThen(exec(currentData -> {
                      log().info("andThen: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
                      self().tell(new MyFsmMessages.ProcessFinished(), self());
                    }))
            )
            /*.anyEvent((event, data) -> {
              log().info("Unknown event: {} {}", event, data);
              return stay();
            })*/
    );

    when(MyFsmState.DONE,
            matchEvent(MyFsmMessages.ProcessFinished.class, (event, data) -> {
              log().info("Done: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
              worker.tell(new Status.Success("I finished!"), self());
              return stop();
            })
            /*.anyEvent((event, data) -> {
              log().info("Unknown event: {} {}", event, data);
              return stay();
            })*/
    );

    whenUnhandled(matchAnyEvent((event, data) -> stay().andThen(exec(d -> log().warning("Unhandled event received {}", event)))));

    onTransition(
      matchState(MyFsmState.IDLE, MyFsmState.STEP_1, this::logTransition)
        .state(MyFsmState.STEP_1, MyFsmState.STEP_2, this::logTransition)
        .state(MyFsmState.STEP_2, MyFsmState.DONE, this::logTransition)
    );
  }

  @Override
  public Class<MyFsmMessages> domainEventClass() {
    return MyFsmMessages.class;
  }

  @Override
  public MyFsmData applyEvent(MyFsmMessages domainEvent, MyFsmData currentData) {
    log().info("applyEvent: event: {} data: {}", domainEvent, currentData);
    MyFsmData newData = currentData;
    if (domainEvent instanceof MyFsmMessages.Step1Started) {
      newData = new MyFsmData("Step1Started");
    } else if (domainEvent instanceof MyFsmMessages.Step2Started) {
      newData = new MyFsmData("Step2Started");
    } else if (domainEvent instanceof MyFsmMessages.ProcessFinished) {
      newData = new MyFsmData("ProcessFinished");
    }
    return newData;
  }

  @Override
  public void onRecoveryCompleted() {
    log().info("onRecoveryCompleted: {} {}", stateName(), stateData());
    self().tell(new MyFsmMessages.ProcessFinished(), self());
  }

  @Override
  public void onReplaySuccess() {
    log().info("onReplaySuccess");
  }

  @Override
  public String persistenceId() {
    return persistenceId;
  }


  private void logTransition() {
    log().info("Transition: stateName: {} stateData(): {} nextStateData(): {}", stateName(), stateData(), nextStateData());
  }
}
