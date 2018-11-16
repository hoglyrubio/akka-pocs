package com.hogly;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public abstract class HelloWorldBot {
  private HelloWorldBot() {
  }

  public static final Behavior<HelloWorld.Greeted> bot(int greetingCounter, int max) {
    return Behaviors.receive((ctx, msg) -> {
      int n = greetingCounter + 1;
      ctx.getLog().info("Greeting {} for {}", n, msg.whom);
      if (n == max) {
        return Behaviors.stopped();
      } else {
        msg.from.tell(new HelloWorld.Greet(msg.whom, ctx.getSelf()));
        return bot(n, max);
      }
    });
  }
}
