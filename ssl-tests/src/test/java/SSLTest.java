import akka.actor.ActorSystem;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.StatusCodes;
import akka.testkit.javadsl.TestKit;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import com.xtiva.nutibara.http.HttpServiceSSLClient;
import com.xtiva.nutibara.http.SSLContextBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.net.ssl.SSLContext;

import java.util.concurrent.ExecutionException;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class SSLTest {

  public static final String KEY_STORE_PATH = "C:\\Users\\hrubiot\\workspace\\nutibara-rx-query\\target\\classes\\certs\\client_server.jks";
  public static final String KEY_STORE_PASSWORD = "123456";
  public static final String TRUST_STORE_PATH = "C:\\Users\\hrubiot\\workspace\\nutibara-rx-query\\target\\classes\\certs\\truststore.jks";
  public static final String TRUST_STORE_PASSWORD = "123456";

  private static final String INSTANCE_ALIAS = "testtenanttest";
  private static final Integer PORT = 34567;
  private static WireMockServer wireMockServer;
  private static ActorSystem system;

  @BeforeClass
  public static void beforeAll() {
    Config config = ConfigFactory.load()
      .withValue("instance.alias", ConfigValueFactory.fromAnyRef(INSTANCE_ALIAS));
    system = ActorSystem.create(INSTANCE_ALIAS, config);

    wireMockServer = new WireMockServer(wireMockConfig()
      .httpsPort(PORT)
      .keystorePath(KEY_STORE_PATH)
      .keystorePassword(KEY_STORE_PASSWORD)
      .trustStorePath(TRUST_STORE_PATH)
      .trustStorePassword(TRUST_STORE_PASSWORD));
    wireMockServer.start();
  }

  @AfterClass
  public static void afterAll() {
    TestKit.shutdownActorSystem(system);
    system = null;
    wireMockServer.stop();
  }

  @Test
  public void test() throws ExecutionException, InterruptedException {
    new TestKit(system) {
      {
        stubFor(get(urlEqualTo("/api/entities/123"))
          .willReturn(aResponse()
            .withStatus(StatusCodes.OK.intValue())
            .withBody("Everything is good")));

        Config config = ConfigFactory.empty()
          .withValue("instance.alias", ConfigValueFactory.fromAnyRef(INSTANCE_ALIAS))
          .withValue("nutibara-ssl.keyStorePath", ConfigValueFactory.fromAnyRef(KEY_STORE_PATH))
          .withValue("nutibara-ssl.keyStorePassword", ConfigValueFactory.fromAnyRef(KEY_STORE_PASSWORD))
          .withValue("nutibara-ssl.trustKeyStorePath", ConfigValueFactory.fromAnyRef(TRUST_STORE_PATH))
          .withValue("nutibara-ssl.trustKeyStorePassword", ConfigValueFactory.fromAnyRef(TRUST_STORE_PASSWORD));
        SSLContext sslContext = new SSLContextBuilder(config).build();

        Http http = Http.get(system);
        http.setDefaultClientHttpsContext(ConnectionContext.https(sslContext));
        HttpServiceSSLClient client = new HttpServiceSSLClient(system, 100, http);
        String response = client.doGet("https://localhost:"+ PORT +"/api/entities/123", String.class).toCompletableFuture().get();

        Assert.assertEquals(response, "Everything is good");
      }
    };
  }

}
