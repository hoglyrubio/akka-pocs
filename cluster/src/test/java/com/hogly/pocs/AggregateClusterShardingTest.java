package com.hogly.pocs;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.javadsl.TestKit;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.apache.commons.lang3.RandomUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.FiniteDuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class AggregateClusterShardingTest {


  public static final FiniteDuration TIMEOUT = FiniteDuration.create(1, TimeUnit.MINUTES);
  public static final String CLUSTER_NAME = "MyClusterName";

  private static Config configNodeA;
  private static ActorSystem systemNodeA;

  private static Config configNodeB;
  private static ActorSystem systemNodeB;

  @BeforeClass
  public static void beforeAll() {
    int portNodeA = RandomUtils.nextInt(6600, 6699);
    int portNodeB = RandomUtils.nextInt(7700, 7799);

    configNodeA = ConfigFactory.load("test-application.conf")
      .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(portNodeA))
      .withValue("akka.cluster.seed-nodes.0", ConfigValueFactory.fromAnyRef("akka.tcp://MyClusterName@127.0.0.1:" + portNodeA))
      .withValue("akka.cluster.seed-nodes.1", ConfigValueFactory.fromAnyRef("akka.tcp://MyClusterName@127.0.0.1:" + portNodeB));

    configNodeB = ConfigFactory.load("test-application.conf")
      .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(portNodeB))
      .withValue("akka.cluster.seed-nodes.0", ConfigValueFactory.fromAnyRef("akka.tcp://MyClusterName@127.0.0.1:" + portNodeA))
      .withValue("akka.cluster.seed-nodes.1", ConfigValueFactory.fromAnyRef("akka.tcp://MyClusterName@127.0.0.1:" + portNodeB));

    systemNodeA = ActorSystem.create(CLUSTER_NAME, configNodeA);
    systemNodeB = ActorSystem.create(CLUSTER_NAME, configNodeB);
  }

  @AfterClass
  public static void afterClass() {
    TestKit.shutdownActorSystem(systemNodeA);
    systemNodeA = null;
    TestKit.shutdownActorSystem(systemNodeB);
    systemNodeB = null;
  }

  @Test
  public void testingASingleActorCluster() {
    new TestKit(systemNodeA) {
      {
        ActorRef actorInNodeA = AggregateClusterSharding.start(systemNodeA, 2);
        ActorRef actorInNodeB = AggregateClusterSharding.start(systemNodeB, 2);

        for (int i = 0; i < 20; i++) {
          send(actorInNodeA, aMessage()); expectMsg(TIMEOUT, Done.getInstance());
          send(actorInNodeB, aMessage()); expectMsg(TIMEOUT, Done.getInstance());
        }
      }
    };
  }

  private AggregateMessage aMessage() {
    AggregateId aggregateId = new AggregateId(UUID.randomUUID().toString());
    return new AggregateMessage(aggregateId, UUID.randomUUID().toString());
  }

}
