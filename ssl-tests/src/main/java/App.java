import akka.actor.ActorSystem;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class App {

  public static void main(String[] args) {

    // mvn exec:java -Dexec.mainClass=App -Dhttps=8081 -Dtcp=2551 -Dhost=localhost -Dport=8082
    // mvn exec:java -Dexec.mainClass=App -Dhttps=8082 -Dtcp=2552 -Dhost=localhost -Dport=8081

    /*int httpPort = Integer.valueOf(Objects.requireNonNull(System.getenv("https"))); // 8081
    int clusteringPort = Integer.valueOf(Objects.requireNonNull(System.getenv("tcp"))); // 2551
    String urlHost = Objects.requireNonNull(System.getenv("host")); // localhost
    String urlPort = Objects.requireNonNull(System.getenv("port")); // 8082
    */
    int httpPort = 8082;            // 8081
    int clusteringPort = 2552;      // 2551
    String urlHost = "localhost";   // localhost
    String urlPort = "8081";        // 8082

    Config config = ConfigFactory.parseString("akka.remote.netty.tcp.port=" + clusteringPort)
      .withFallback(ConfigFactory.load());
    ActorSystem system = ActorSystem.create("MyClusterName", config);
    SSLDirective.start(system, "localhost", httpPort, "https://" + urlHost + ":" + urlPort + "/entities/{0}");
    EntityClusterSharding.createClusterShardingActor(system);
  }

}
