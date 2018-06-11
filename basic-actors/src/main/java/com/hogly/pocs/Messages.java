package com.hogly.pocs;

public class Messages {

  public static class ExecuteSomething {
    public static ExecuteSomething getInstance() {
      return new ExecuteSomething();
    }
  }

  public static class ExecuteWithStoppableException {
    public static ExecuteWithStoppableException getInstance() {
      return new ExecuteWithStoppableException();
    }
  }

  public static class ExecuteWithResumableException {
    public static ExecuteWithResumableException getInstance() {
      return new ExecuteWithResumableException();
    }
  }

  public static class ExecuteWithRestartableException {
    public static ExecuteWithRestartableException getInstance() {
      return new ExecuteWithRestartableException();
    }
  }
}
