package com.hogly;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.testkit.TestActor;
import akka.testkit.TestProbe;
import akka.testkit.javadsl.TestKit;
import org.junit.Test;

import java.util.Arrays;

public class SimpleStreamingTest {

  private static ActorSystem system = ActorSystem.create();

  @Test
  public void test1() {
    new TestKit(system) {
      {
        Source<Double, NotUsed> source = Source.from(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0, 6.0));
        TestProbe probe = TestProbe.apply(system);

        probe.setAutoPilot(new TestActor.AutoPilot() {
          @Override
          public TestActor.AutoPilot run(ActorRef sender, Object msg) {
            if (msg instanceof AggregateActor.GetValue) {

            }
            return noAutoPilot();
          }
        });

        source
          .via(SimpleStreaming.flows(system, probe.ref()))
          .runWith(Sink.ignore(), ActorMaterializer.create(system));

      }
    };
  }

}
