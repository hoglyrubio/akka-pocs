import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.HttpsConnectionContext;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.util.ByteString;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class HttpsClient {

  private final ActorSystem system;
  private final Http http;
  private final ActorMaterializer materializer;
  private final SSLContext sslContext;
  private final HttpsConnectionContext httpsConnectionContext;

  public static void main(String[] args) throws Exception {
    System.out.println(LocalDateTime.now());
    ActorSystem system = ActorSystem.create();
    HttpsClient client = new HttpsClient(system);
    for (int i = 0; i < 100; i++) {
      Pair<StatusCode, String> pair = client.doGet("https://localhost:8080/hello")
        .thenCompose(httpResponse -> client.toStatusAndBody(httpResponse))
        .toCompletableFuture().get(10, TimeUnit.SECONDS);
      System.out.println(LocalDateTime.now() + " - " + i + " - status: " + pair.first() + " response: " + pair.second());
    }
  }

  public HttpsClient(ActorSystem system) {
    this.system = system;
    this.http = Http.get(system);
    this.materializer = ActorMaterializer.create(system);
    this.sslContext = new SSLContextBuilder()
      .setKeyStoreResourcePath("certs/my-keystore.jks")
      .setKeyStorePassword("123456")
      .setTrustKeyStoreResourcePath("certs/my-truststore.jks")
      .setTrustKeyStorePassword("123456")
      .build();
    this.httpsConnectionContext = ConnectionContext.https(sslContext);
  }

  public CompletionStage<HttpResponse> doGet(String url) {
    HttpRequest request = HttpRequest.create(url);
    return http.singleRequest(request, httpsConnectionContext, materializer);
  }

  public CompletionStage<Pair<StatusCode, String>> toStatusAndBody(HttpResponse httpResponse) {
    return httpResponse.entity()
      .toStrict(10000, materializer)
      .thenApply(strict -> Pair.create(httpResponse.status(), strict.getData().utf8String()));
  }

}
