import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.cluster.sharding.ClusterSharding;
import akka.cluster.sharding.ClusterShardingSettings;
import akka.cluster.sharding.ShardRegion;

public class EntityClusterSharding {

  public static final String SHARD_REGION_NAME = "ENTITY";
  public static final int MAX_NUMBER_OF_SHARDS = 10;

  public static ActorRef createClusterShardingActor(ActorSystem system) {
    return createClusterShardingActor(system, SHARD_REGION_NAME, MAX_NUMBER_OF_SHARDS);
  }

  public static ActorRef createClusterShardingActor(ActorSystem system, String regionName, int maxNumberOfShards) {

    ClusterShardingSettings clusterShardingSettings = ClusterShardingSettings.create(system);

    ShardRegion.MessageExtractor messageExtractor = new ShardRegion.HashCodeMessageExtractor(maxNumberOfShards) {
      @Override
      public String entityId(Object msg) {
        if (msg instanceof EntityMessage) {
          return ((EntityMessage) msg).getEntityId().id();
        }
        return null;
      }
    };

    return ClusterSharding.get(system).start(
      regionName,
      Props.create(EntityActor.class),
      clusterShardingSettings,
      messageExtractor
    );
  }

  public static ActorRef clusteredActor(ActorSystem system) {
    return ClusterSharding.get(system).shardRegion(SHARD_REGION_NAME);
  }

}
