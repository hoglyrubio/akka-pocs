import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class App {

  public static void main(String[] args) {

    /**
     * 1) To execute the first instance use: httpPort=8081 and akkaPort=2551
     * 2) To execute the second instance use: httpPort=8082 and akkaPort=2552
     * 3) Try calling the endpoints defined in SSLDirective class:
     */

    int httpPort = 8082; // [ 8081, 8082 ]
    int akkaPort = 2552; // [ 2551, 2552 ]

    Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + akkaPort)
      .withFallback(ConfigFactory.load());
    ActorSystem system = ActorSystem.create("MyClusterName", config);
    SSLDirective.start(system, "localhost", httpPort);
    EntityClusterSharding.createClusterShardingActor(system);
  }

}
