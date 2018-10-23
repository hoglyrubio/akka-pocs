package com.hogly;

import java.util.Random;

public class SleepUtils {

  private static final Random RANDOM = new Random();

  public static void sleep() {
    try {
      Thread.sleep(RANDOM.nextInt(100));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public static void sleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

}
