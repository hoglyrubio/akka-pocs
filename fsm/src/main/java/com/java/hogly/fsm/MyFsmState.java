package com.java.hogly.fsm;

import akka.persistence.fsm.PersistentFSM;

public enum MyFsmState implements PersistentFSM.FSMState {

  IDLE,
  STEP_1,
  STEP_2,
  DONE,
  FAILED;

  @Override
  public String identifier() {
    return this.name();
  }
}
