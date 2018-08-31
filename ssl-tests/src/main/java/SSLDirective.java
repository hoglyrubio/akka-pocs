import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.HttpsConnectionContext;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;

import javax.net.ssl.SSLContext;
import java.io.IOException;

public class SSLDirective extends AllDirectives {

  public static HttpsConnectionContext createHttpsConnectionContext() {
    SSLContext sslContext = new SSLContextBuilder()
      .setKeyStoreResourcePath("certs/my-keystore.jks")
      .setKeyStorePassword("123456")
      .setTrustKeyStoreResourcePath("certs/my-truststore.jks")
      .setTrustKeyStorePassword("123456")
      .build();
    return ConnectionContext.https(sslContext);
  }

  public Route createRoute() {
    return path("hello", () -> complete("Hi everyone!"));
  }

  public static void main(String[] args) throws IOException {
    final ActorSystem system = ActorSystem.create("SimpleServerApp");
    final ActorMaterializer materializer = ActorMaterializer.create(system);
    final Http http = Http.get(system);

    boolean useHttps = true;
    if ( useHttps ) {
      HttpsConnectionContext https = createHttpsConnectionContext();
      http.setDefaultServerHttpContext(https);
    }

    final SSLDirective app = new SSLDirective();
    final Flow<HttpRequest, HttpResponse, NotUsed> flow = app.createRoute().flow(system, materializer);

    Http.get(system).bindAndHandle(flow, ConnectHttp.toHost("localhost", 8080), materializer);

    System.out.println("Type RETURN to exit");
    System.in.read();
    system.terminate();
  }
}
