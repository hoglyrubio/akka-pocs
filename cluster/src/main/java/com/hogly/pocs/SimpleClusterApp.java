package com.hogly.pocs;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import akka.cluster.singleton.ClusterSingletonManager;
import akka.cluster.singleton.ClusterSingletonManagerSettings;
import akka.cluster.singleton.ClusterSingletonProxy;
import akka.cluster.singleton.ClusterSingletonProxySettings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

public class SimpleClusterApp {

  public static void main(String[] args) {
    if (args.length == 0)
      startup(new String[] { "2551", "2552", "0" });
    else
      startup(args);
  }

  public static void startup(String[] ports) {
    for (String port : ports) {
      Config config = ConfigFactory.parseString(
        "akka.remote.netty.tcp.port=" + port + "\n" +
          "akka.remote.artery.canonical.port=" + port)
        .withFallback(ConfigFactory.load());

      ActorSystem system = ActorSystem.create("MyClusterName", config);

      //startClusterActor(system);
      //startClusterSingletonActor(system);
      startClusterShardingActors(system);
    }
  }

  private static void startClusterShardingActors(ActorSystem system) {
    ClusterShardingSettings settings = ClusterShardingSettings.create(system);

    ShardRegion.MessageExtractor messageExtractor = getMessageExtractor();

    ActorRef shardRegion = ClusterSharding.get(system).start(
      "Aggregate",
      Props.create(AggregateActor.class),
      settings,
      messageExtractor
    );

    ActorRef aggregate = ClusterSharding.get(system).shardRegion("Aggregate");

    system.scheduler().schedule(
      FiniteDuration.apply(5, TimeUnit.SECONDS),
      FiniteDuration.apply(5, TimeUnit.SECONDS),
      () -> {
        AggregateId aggregateId = AggregateId.create();
        AggregateMessage<String> message = new AggregateMessage<>(aggregateId, "Hola");
        aggregate.tell(message, ActorRef.noSender());
      },
      system.dispatcher()
    );

  }

  private static ShardRegion.MessageExtractor getMessageExtractor() {
    return new ShardRegion.MessageExtractor() {
      @Override
      public String entityId(Object msg) {
        if (msg instanceof AggregateMessage) {
          return ((AggregateMessage) msg).getAggregateId().id();
        }
        return null;
      }

      @Override
      public Object entityMessage(Object msg) {
        if (msg instanceof AggregateMessage) {
          return ((AggregateMessage) msg).getPayload();
        }
        return msg;
      }

      @Override
      public String shardId(Object msg) {
        if (msg instanceof AggregateMessage) {
          AggregateId aggregateId = ((AggregateMessage) msg).getAggregateId();
          int shardId = Math.abs(aggregateId.hashCode() % 2);
          return String.valueOf(shardId);
        }
        return null;
      }

    };
  }

  private static void startClusterSingletonActor(ActorSystem system) {
    ClusterSingletonManagerSettings settings = ClusterSingletonManagerSettings.create(system);
    ActorRef singleton = system.actorOf(
      ClusterSingletonManager.props(Props.create(SingletonActor.class), Done.getInstance(), settings),
      "mySingleton"
    );
    system.log().info("SINGLETON: {}", singleton);

    ClusterSingletonProxySettings proxySettings = ClusterSingletonProxySettings.create(system);
    ActorRef proxy = system.actorOf(ClusterSingletonProxy.props("/user/mySingleton", proxySettings), "mySingletonProxy");
    system.log().info("PROXY: {}", singleton);

    system.scheduler().schedule(
      FiniteDuration.apply(5, TimeUnit.SECONDS),
      FiniteDuration.apply(5, TimeUnit.SECONDS),
      () -> proxy.tell("Hi singleton!", ActorRef.noSender()),
      system.dispatcher()
    );
  }

  private static void startClusterActor(ActorSystem system) {
    // Create an actor that handles cluster domain events
    system.actorOf(Props.create(SimpleClusterListener.class),"clusterListener");
  }
}
