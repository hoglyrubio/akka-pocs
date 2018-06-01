package com.java.hogly.fsm;

import java.io.Serializable;

public class MyFsmMessages implements Serializable {

  public static class StartProcess extends MyFsmMessages {
    @Override
    public String toString() {
      return "StartProcess{}";
    }
  }

  public static class StartStep1 extends MyFsmMessages {
    @Override
    public String toString() {
      return "StartStep1{}";
    }
  }

  public static class Step1Started extends MyFsmMessages {
    @Override
    public String toString() {
      return "Step1Started{}";
    }
  }

  public static class Step1Finished extends MyFsmMessages {
    @Override
    public String toString() {
      return "Step1Finished{}";
    }
  }

  public static class StartStep2 extends MyFsmMessages {
    @Override
    public String toString() {
      return "StartStep2{}";
    }
  }

  public static class Step2Started extends MyFsmMessages {
    @Override
    public String toString() {
      return "Step2Started{}";
    }
  }

  public static class Step2Finished extends MyFsmMessages {
    @Override
    public String toString() {
      return "Step2Finished{}";
    }
  }

  public static class ProcessFinished extends MyFsmMessages {
    @Override
    public String toString() {
      return "StartProcess{}";
    }
  }
}
