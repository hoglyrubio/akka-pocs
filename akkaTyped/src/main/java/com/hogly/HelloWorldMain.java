package com.hogly;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;

public abstract class HelloWorldMain {
  private HelloWorldMain() {
  }

  public static class Start {
    public final String name;

    public Start(String name) {
      this.name = name;
    }
  }

  public static final Behavior<Start> main =
    Behaviors.setup(context -> {
      final ActorRef<HelloWorld.Greet> greeter =
        context.spawn(HelloWorld.greeter, "greeter");

      return Behaviors.receiveMessage(msg -> {
        ActorRef<HelloWorld.Greeted> replyTo =
          context.spawn(HelloWorldBot.bot(0, 3), msg.name);
        greeter.tell(new HelloWorld.Greet(msg.name, replyTo));
        return Behaviors.same();
      });
    });
}