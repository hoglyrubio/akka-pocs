package com.java.hogly.fsm;

import akka.actor.ActorRef;
import akka.actor.Status;
import akka.persistence.fsm.AbstractPersistentFSM;

public class MyPersistentFsm extends AbstractPersistentFSM<MyFsmState, MyFsmData, MyFsmMessages> {

  private static String persistenceId;
  private static ActorRef worker;
  private static MyFsmData myData;

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
                      worker.tell(new MyFsmMessages.StartStep1(), self());
                    }))
            )
    );

    when(MyFsmState.STEP_1,
            matchEvent(MyFsmMessages.Step1Finished.class, (event, data) -> goTo(MyFsmState.STEP_2)
                    .applying(new MyFsmMessages.Step2Started())
                    .andThen(exec(currentData -> {
                      log().info("andThen: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
                      worker.tell(new MyFsmMessages.StartStep2(), self());
                    }))
            )
    );

    when(MyFsmState.STEP_2,
            matchEvent(MyFsmMessages.Step2Finished.class, (event, data) -> goTo(MyFsmState.DONE)
                    .applying(new MyFsmMessages.ProcessFinished())
                    .andThen(exec(currentData -> {
                      log().info("andThen: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
                      self().tell(new MyFsmMessages.ProcessFinished(), self());
                    }))
            )
    );

    when(MyFsmState.DONE,
            matchEvent(MyFsmMessages.ProcessFinished.class, (event, data) -> {
              log().info("Done: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
              worker.tell(new Status.Success());
              return stop();
            })
    );

  }

  @Override
  public Class<MyFsmMessages> domainEventClass() {
    return MyFsmMessages.class;
  }

  @Override
  public MyFsmData applyEvent(MyFsmMessages domainEvent, MyFsmData currentData) {
    log().info("applyEvent: event: {} data: {}", domainEvent, currentData);
    return currentData;
  }

  @Override
  public String persistenceId() {
    return persistenceId;
  }
}
