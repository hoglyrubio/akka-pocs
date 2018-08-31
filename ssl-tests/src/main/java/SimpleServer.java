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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Optional;

public class SimpleServer extends AllDirectives {

  public static HttpsConnectionContext useHttps(ActorSystem system) {
    HttpsConnectionContext https = null;
    try {
      KeyStore keyStore = KeyStore.getInstance("JKS");
      InputStream keystore = read("certs/my-keystore.jks");
      keyStore.load(keystore, "123456".toCharArray());

      KeyStore trustedKey = KeyStore.getInstance("JKS");
      InputStream trustStore = read("certs/my-truststore.jks");
      trustedKey.load(trustStore, "123456".toCharArray());

      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
      keyManagerFactory.init(keyStore, "123456".toCharArray());

      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
      trustManagerFactory.init(trustedKey);

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new SecureRandom());

      https = ConnectionContext.https(sslContext);

    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      system.log().error(e, "Exception while configuring HTTPS.");
    } catch (CertificateException | KeyStoreException | UnrecoverableKeyException | IOException e) {
      system.log().error(e,"Exception while ");
    }

    return https;
  }

  public static InputStream read(String path) {
    return Optional.ofNullable(SimpleClient.class.getClassLoader().getResourceAsStream(path))
      .orElseThrow(() -> new RuntimeException("Not found: " + path));
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
      HttpsConnectionContext https = useHttps(system);
      http.setDefaultServerHttpContext(https);
    }

    final SimpleServer app = new SimpleServer();
    final Flow<HttpRequest, HttpResponse, NotUsed> flow = app.createRoute().flow(system, materializer);

    Http.get(system).bindAndHandle(flow, ConnectHttp.toHost("localhost", 8080), materializer);

    System.out.println("Type RETURN to exit");
    System.in.read();
    system.terminate();
  }
}
