package com.hogly.cluster.entities;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.pattern.PatternsCS;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import scala.concurrent.duration.FiniteDuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class ClusterShardingApp {

  public static void main(String[] args) {

    String CLUSTER_NAME = "MyClusterName";

    int portNodeA = 7777;
    int portNodeB = 8888;
    int port = portNodeA;

    Config config = ConfigFactory.load("test-application.conf")
      .withValue("akka.remote.netty.tcp.port", ConfigValueFactory.fromAnyRef(port))
      .withValue("akka.cluster.seed-nodes.0", ConfigValueFactory.fromAnyRef("akka.tcp://" + CLUSTER_NAME + "@127.0.0.1:" + portNodeA))
      .withValue("akka.cluster.seed-nodes.1", ConfigValueFactory.fromAnyRef("akka.tcp://" + CLUSTER_NAME + "@127.0.0.1:" + portNodeB));

    ActorSystem system = ActorSystem.create(CLUSTER_NAME, config);
    ActorRef actor = start(system, 2);

    system.scheduler().schedule(
      FiniteDuration.apply(5, TimeUnit.SECONDS),
      FiniteDuration.apply(5, TimeUnit.SECONDS),
      () -> {
        PatternsCS.ask(actor, aMessage(), 30000)
          .thenAccept(response -> system.log().info("response: {}", response))
          .exceptionally(t -> {
            system.log().error(t, "Error sending message to {}", actor);
            return null;
          });
      },
      system.dispatcher()
    );
  }

  private static AggregateMessage aMessage() {
    AggregateId aggregateId = new AggregateId(UUID.randomUUID().toString());
    return new AggregateMessage(aggregateId, UUID.randomUUID().toString());
  }


  public static ActorRef start(ActorSystem system, int maxShards) {

    ClusterSharding clusterSharding = ClusterSharding.get(system);
    ClusterShardingSettings clusterShardingSettings = ClusterShardingSettings.create(system);
    Props entityProps = Props.create(AggregateActor.class);

    return clusterSharding.start("Aggregate", entityProps, clusterShardingSettings,
      new ShardRegion.HashCodeMessageExtractor(maxShards) {
        @Override
        public String entityId(Object msg) {
          if (msg instanceof AggregateMessage) {
            return ((AggregateMessage) msg).getAggregateId().id();
          }
          return null;
        }
      }
    );
  }

}
