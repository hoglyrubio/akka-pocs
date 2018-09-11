import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.HttpsConnectionContext;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.pattern.PatternsCS;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import javax.net.ssl.SSLContext;
import java.text.MessageFormat;
import java.util.concurrent.CompletionStage;

public class SSLDirective extends AllDirectives {

  private final ActorSystem system;
  private final HttpsClient httpsClient;

  public static CompletionStage<ServerBinding> start(ActorSystem system, String hostname, int port) {
    Http http = Http.get(system);
    ActorMaterializer materializer = ActorMaterializer.create(system);
    HttpsConnectionContext httpsConnectionContext = createHttpsConnectionContext();
    http.setDefaultServerHttpContext(httpsConnectionContext);
    SSLDirective app = new SSLDirective(system);
    Flow<HttpRequest, HttpResponse, NotUsed> flow = app.createRoute().flow(system, materializer);
    return Http.get(system).bindAndHandle(flow, ConnectHttp.toHost(hostname, port), materializer);
  }

  private SSLDirective(ActorSystem system) {
    this.system = system;
    this.httpsClient = new HttpsClient(system);
  }

  public Route createRoute() {
    return route(
      // e.g. GET /entity/{entityId}
      get(() -> path(PathMatchers.segment("entities").slash(PathMatchers.segment()), id -> handleEntity(id))),
      // e.g. GET /sayhello?host=localhost&port=8082
      get(() -> path("sayHello", () -> parameter("host", host -> parameter("port", port -> sayHello(host, port))))),
      // e.g. GET /hello
      get(() -> path("hello", () -> complete("Hi")))
    );
  }

  private Route sayHello(String host, String port) {
    String url = "https://" + host + "/" + port + "/hello";

    CompletionStage<String> response = httpsClient.doGet(MessageFormat.format(url, EntityId.create()))
      .thenCompose(httpResponse -> httpsClient.toStatusAndBody(httpResponse))
      .thenApply(pair -> "Received: " + pair.second() + " from: " + url + " status: " + pair.first())
      .exceptionally(e -> {
        system.log().error(e, "Error in callback");
        return null;
      });

    return completeOKWithFutureString(response);
  }

  private Route handleEntity(String id) {
    ActorRef clusterShardingActor = EntityClusterSharding.clusteredActor(system);
    EntityMessage message = new EntityMessage(new EntityId(id), "Hi");
    CompletionStage<String> future = PatternsCS.ask(clusterShardingActor, message, 10000)
      .thenApply(obj -> EntityId.class.cast(obj))
      .thenApply(EntityId::id);
    return completeOKWithFutureString(future);
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

}
