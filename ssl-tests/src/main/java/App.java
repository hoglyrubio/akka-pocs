import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class App {

  public static void main(String[] args) {
    Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + 2552)
      .withFallback(ConfigFactory.load());
    ActorSystem system = ActorSystem.create("MyClusterName", config);
    SSLDirective.start(system, "localhost", 82, "https://localhost:8081/entities/{0}");
    createClusterShardingActor(system, "ENTITY", 2);
  }

  private static ActorRef createClusterShardingActor(ActorSystem system, String regionName, int maxNumberOfShards) {

    ClusterShardingSettings clusterShardingSettings = ClusterShardingSettings.create(system);

    ShardRegion.MessageExtractor messageExtractor = new ShardRegion.HashCodeMessageExtractor(maxNumberOfShards) {
      @Override
      public String entityId(Object msg) {
        if (msg instanceof AggregateMessage) {
          return ((AggregateMessage) msg).getAggregateId().id();
        }
        return null;
      }
    };

    return ClusterSharding.get(system).start(
      regionName,
      Props.create(AggregateActor.class),
      clusterShardingSettings,
      messageExtractor
    );

    //ActorRef aggregate = ClusterSharding.get(system).shardRegion("Aggregate");
  }

}
