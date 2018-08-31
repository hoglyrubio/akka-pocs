import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.sharding.ClusterSharding;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.HttpsConnectionContext;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.japi.Pair;
import akka.pattern.PatternsCS;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.CompletionStage;

public class SSLDirective extends AllDirectives {

  private final String responseUrl;
  private final ActorSystem system;
  private final HttpsClient httpsClient;

  public static CompletionStage<ServerBinding> start(ActorSystem system, String hostname, int port, String responseUrl) {
    Http http = Http.get(system);
    ActorMaterializer materializer = ActorMaterializer.create(system);
    HttpsConnectionContext httpsConnectionContext = createHttpsConnectionContext();
    http.setDefaultServerHttpContext(httpsConnectionContext);
    SSLDirective app = new SSLDirective(system, responseUrl);
    Flow<HttpRequest, HttpResponse, NotUsed> flow = app.createRoute().flow(system, materializer);
    return Http.get(system).bindAndHandle(flow, ConnectHttp.toHost(hostname, port), materializer);
  }

  private SSLDirective(ActorSystem system, String responseUrl) {
    this.system = system;
    this.httpsClient = new HttpsClient(system);
    this.responseUrl = responseUrl;
  }

  public Route createRoute() {
    return path(
      PathMatchers.segment("entities").slash(PathMatchers.segment()), id -> handleEntity(id)
    );
  }

  private Route handleEntity(String id) {
    ActorRef clusteredActor = ClusterSharding.get(system).shardRegion("ENTITY");
    AggregateMessage message = new AggregateMessage(new AggregateId(id), "Hi");
    CompletionStage<String> future = PatternsCS.ask(clusteredActor, message, 10000)
      .thenApply(obj -> AggregateId.class.cast(obj))
      .thenApply(AggregateId::id);
    //callbackUsingHttps();
    return completeOKWithFutureString(future);
  }

  private void callbackUsingHttps() {
    httpsClient.doGet(MessageFormat.format(responseUrl, AggregateId.create()))
      .thenCompose(httpResponse -> httpsClient.toStatusAndBody(httpResponse))
      .thenAccept(pair -> system.log().info("Received response: {} {} ", pair.first(), pair.second()))
      .exceptionally(e -> {
        system.log().error(e, "Error in callback");
        return null;
      });
  }

  public static HttpsConnectionContext createHttpsConnectionContext() {
    SSLContext sslContext = new SSLContextBuilder()
      .setKeyStoreResourcePath("certs/my-keystore.jks")
      .setKeyStorePassword("123456")
      .setTrustKeyStoreResourcePath("certs/my-truststore.jks")
      .setTrustKeyStorePassword("123456")
      .build();
    return ConnectionContext.https(sslContext);
  }

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("my-ssl-app");
    SSLDirective.start(system, "localhost", 8080, "https://localhost:8081/entities/{0}");
  }
}
