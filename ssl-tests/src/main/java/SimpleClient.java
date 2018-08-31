import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.HttpsConnectionContext;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
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

public class SimpleClient {

  public static void main(String[] args) throws Exception {
    ActorSystem system = ActorSystem.create();
    System.out.println(LocalDateTime.now());
    ActorMaterializer materializer = ActorMaterializer.create(system);
    Http http = Http.get(system);

    for (int i = 0; i < 100; i++) {
      HttpResponse response = makeRequest(http, materializer, "https://localhost:8080/hello")
        .toCompletableFuture().get(10, TimeUnit.SECONDS);

      CompletionStage<String> body = response.entity()
        .toStrict(1000L, materializer)
        .thenApplyAsync(strict -> {
          ByteString data = strict.getData();
          return data.utf8String();
        });

      System.out.println(LocalDateTime.now() + " - " + i + " - " + response.status() + " response: " + body.toCompletableFuture().get());
    }
  }

  private static CompletionStage<HttpResponse> makeRequest(Http http, ActorMaterializer materializer, String url) throws Exception {
    HttpRequest request = HttpRequest.create(url);
    HttpsConnectionContext context = getHttpsConnectionContext();
    return http.singleRequest(request, context, materializer);
  }

  public static HttpsConnectionContext getHttpsConnectionContext() throws Exception {
    KeyStore keyStore = KeyStore.getInstance("JKS");
    InputStream inputStream = read("certs/my-keystore.jks");
    keyStore.load(inputStream, "123456".toCharArray());

    KeyStore trustedKey = KeyStore.getInstance("JKS");
    InputStream trustStore = read("certs/my-truststore.jks");
    trustedKey.load(trustStore, "123456".toCharArray());

    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
    keyManagerFactory.init(keyStore, "123456".toCharArray());

    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
    trustManagerFactory.init(trustedKey);

    SSLContext context = SSLContext.getInstance("TLS");
    context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

    return ConnectionContext.https(context);
  }

  public static InputStream read(String path) {
    return Optional.ofNullable(SimpleClient.class.getClassLoader().getResourceAsStream(path))
      .orElseThrow(() -> new RuntimeException("Not found: " + path));
  }

}
