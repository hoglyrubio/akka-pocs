package com.java.hogly.fsm;

public class MyFsmMessages {

  public static class StartProcess {
    @Override
    public String toString() {
      return "StartProcess{}";
    }
  }

  public static class Step1Finished {
    @Override
    public String toString() {
      return "Step1Finished{}";
    }
  }

  public static class Step2Finished {
    @Override
    public String toString() {
      return "Step2Finished{}";
    }
  }


}
