package com.java.hogly.fsm;

import akka.Done;
import akka.actor.AbstractLoggingFSM;
import akka.actor.ActorRef;
import akka.actor.Status;
import scala.Option;

public class MyFsm extends AbstractLoggingFSM<MyFsmState, MyFsmData> {

  private static ActorRef parentActor;

  public MyFsm(ActorRef parentActor) {
    this.parentActor = parentActor;
    log().info("CONSTRUCTOR: {}", parentActor);
  }

  {
    startWith(MyFsmState.IDLE, new MyFsmData("I'm idle"));

    when(MyFsmState.IDLE, matchEvent(MyFsmMessages.StartProcess.class, (event, data) -> {
      log().info("Incoming: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
      return goTo(MyFsmState.STEP_1).using(new MyFsmData("I'm in step 1"));
    }));

    when(MyFsmState.STEP_1, matchEvent(MyFsmMessages.Step1Finished.class, (event, data) -> {
      log().info("Incoming: event {} data {}. stateData: {} stateName: {}", event, data, stateData(), stateName());
      return goTo(MyFsmState.STEP_2).using(new MyFsmData("I'm in step 2"));
    }));

    when(MyFsmState.STEP_2, matchEvent(MyFsmMessages.Step2Finished.class, (event, data) -> {
      log().info("Incoming: event {} data {}. stateData(): {} stateName(): {}", event, data, stateData(), stateName());
      throw new RuntimeException("Exception in the middle");
      //return goTo(MyFsmState.DONE).using(new MyFsmData("I'm Done"));
    }));

    when(MyFsmState.DONE, matchEvent(Done.class, (event, data) -> {
      log().info("Incoming: event {} data {}. stateData(): {} stateName(): {}", event, data, stateData(), stateName());
      log().info("Finished work, stopping FSM");
      parentActor.tell(new Status.Success(data), self());
      return stop();
    }));

    whenUnhandled(matchAnyEvent((event, data) -> {
      log().error("ERROR Incoming: event {} data {}. stateData(): {} stateName(): {}", event, data, stateData(), stateName());
      parentActor.tell(new Status.Failure(new RuntimeException("Some error")), self());
      return goTo(MyFsmState.FAILED);
    }));

    onTransition(
            matchState(MyFsmState.IDLE, MyFsmState.STEP_1, () -> {
              log().info("Transition: stateName: {} stateData(): {} nextStateData(): {}", stateName(), stateData(), nextStateData());
              log().info("Doing some work in step 1 and answering...");
              self().tell(new MyFsmMessages.Step1Finished(), self());
            })
            .state(MyFsmState.STEP_1, MyFsmState.STEP_2, () -> {
              log().info("Transition: stateName: {} stateData(): {} nextStateData(): {}", stateName(), stateData(), nextStateData());
              log().info("Doing some work in step 2 and answering...");
              self().tell(new MyFsmMessages.Step2Finished(), self());
            })
            .state(MyFsmState.STEP_2, MyFsmState.DONE, () -> {
              log().info("Transition: stateName: {} stateData(): {} nextStateData(): {}", stateName(), stateData(), nextStateData());
              log().info("Finishing work on FSM");
              self().tell(Done.getInstance(), self());
            })
            .state(MyFsmState.IDLE, MyFsmState.FAILED, () -> {
              log().info("Transition: stateName: {} stateData(): {} nextStateData(): {}", stateName(), stateData(), nextStateData());
            })
            .state(MyFsmState.STEP_1, MyFsmState.FAILED, () -> {
              log().info("Transition: stateName: {} stateData(): {} nextStateData(): {}", stateName(), stateData(), nextStateData());
            })
            .state(MyFsmState.STEP_2, MyFsmState.FAILED, () -> {
              log().info("Transition: stateName: {} stateData(): {} nextStateData(): {}", stateName(), stateData(), nextStateData());
            })
            .build()
    );

    onTermination(
            matchStop(Normal(), (state, data) -> {
              log().warning("onTermination. Normal() state: {} data: {}", state, data);
            })
            .stop(Shutdown(), (state, data) -> {
              log().warning("onTermination. Shutdown() state: {} data: {}", state, data);
            })
            .stop(Failure.class, (reason, state, data) -> {
              log().warning("onTermination. reason: {} state: {} data: {}", reason, state, data);
            })
    );

    initialize();
  }

  @Override
  public void postStop() {
    log().warning("POST-STOP");
  }

  @Override
  public void postRestart(Throwable reason) throws Exception {
    log().error(reason, "POST-RESTART");
  }

  @Override
  public void preStart() throws Exception {
    log().warning("PRE-START");
  }

}
