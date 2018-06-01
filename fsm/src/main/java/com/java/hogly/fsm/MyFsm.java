package com.java.hogly.fsm;

import akka.Done;
import akka.actor.AbstractFSM;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class MyFsm extends AbstractFSM<MyFsmState, MyFsmData> {

  {
    startWith(MyFsmState.IDLE, new MyFsmData("I'm idle"));

    when(MyFsmState.IDLE, matchEvent(MyFsmMessages.StartProcess.class, (msg, data) -> {
      log().info("Incoming: msg {} data {}. stateData: {} stateName: {}", msg, data, stateData(), stateName());
      return goTo(MyFsmState.STEP_1).using(new MyFsmData("I'm in step 1"));
    }));

    when(MyFsmState.STEP_1, matchEvent(MyFsmMessages.Step1Finished.class, (msg, data) -> {
      log().info("Incoming: msg {} data {}. stateData: {} stateName: {}", msg, data, stateData(), stateName());
      return goTo(MyFsmState.STEP_2).using(new MyFsmData("I'm in step 2"));
    }));

    when(MyFsmState.STEP_2, matchEvent(MyFsmMessages.Step2Finished.class, (msg, data) -> {
      log().info("Incoming: msg {} data {}. stateData(): {} stateName(): {}", msg, data, stateData(), stateName());
      return goTo(MyFsmState.DONE).using(new MyFsmData("I'm Done"));
    }));

    when(MyFsmState.DONE, matchEvent(Done.class, (msg, data) -> {
      log().info("Incoming: msg {} data {}. stateData(): {} stateName(): {}", msg, data, stateData(), stateName());
      log().info("Finished work, stopping FSM");
      return stop();
    }));

    whenUnhandled(matchAnyEvent((msg, data) -> {
      log().error("ERROR Incoming: msg {} data {}. stateData(): {} stateName(): {}", msg, data, stateData(), stateName());
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
            .build()
    );

    initialize();
  }

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create();
    ActorRef myFsm = system.actorOf(Props.create(MyFsm.class));
    myFsm.tell(new MyFsmMessages.StartProcess(), ActorRef.noSender());
  }
}
