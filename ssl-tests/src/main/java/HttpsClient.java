import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import akka.http.javadsl.ConnectionContext;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpMethods;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCode;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.OverflowStrategy;
import akka.stream.QueueOfferResult;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import scala.compat.java8.FutureConverters;
import scala.concurrent.Promise;
import scala.util.Try;

import javax.net.ssl.SSLContext;
import java.util.Arrays;
import java.util.concurrent.CompletionStage;

public class HttpsClient {

  protected final ActorSystem system;
  protected final Http http;
  protected final ActorMaterializer materializer;
  protected final SSLContext sslContext;
  protected final int bufferSize = 5000;
  protected final SourceQueue<Pair<HttpRequest, Promise<HttpResponse>>> sourceQueue;
  protected final ObjectMapper MAPPER = new ObjectMapper()
    .registerModules(new Jdk8Module(), new JavaTimeModule(), new ParameterNamesModule(JsonCreator.Mode.PROPERTIES))
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  public HttpsClient(ActorSystem system) {
    this.system = system;
    this.sslContext = new SSLContextBuilder()
      .setKeyStoreResourcePath("certs/my-keystore.jks")
      .setKeyStorePassword("123456")
      .setTrustKeyStoreResourcePath("certs/my-truststore.jks")
      .setTrustKeyStorePassword("123456")
      .build();
    this.http = Http.get(system);
    this.http.setDefaultClientHttpsContext(ConnectionContext.https(sslContext));
    this.materializer = ActorMaterializer.create(system);
    this.sourceQueue = sourceQueue();
  }

  public CompletionStage<HttpResponse> doGetSingleRequest(String url, HttpHeader... headers) {
    HttpRequest request = HttpRequest.create(url)
      .withMethod(HttpMethods.GET)
      .addHeaders(Arrays.asList(headers));
    return http.singleRequest(request, materializer);
  }

  public CompletionStage<HttpResponse> doGetSuperPool(String url, HttpHeader... headers) {

    HttpRequest request = HttpRequest.create(url)
      .withMethod(HttpMethods.GET)
      .addHeaders(Arrays.asList(headers));

    system.log().info("REQUEST: {} {}", request.method(), request.getUri());

    Promise<HttpResponse> promise = Futures.promise();
    CompletionStage<HttpResponse> httpResponseFuture = sourceQueue.offer(Pair.create(request, promise))
      .thenCompose(queueOfferResult -> {
        if (queueOfferResult instanceof QueueOfferResult.Enqueued$) {
          return FutureConverters.toJava(promise.future());
        }
        throw new RuntimeException("queueOfferResult is not instance of QueueOfferResult.Enqueued$: " + queueOfferResult);
      });

    httpResponseFuture.thenAccept(response -> system.log().info("RESPONSE: {} {}", response.status(), request.getUri()));

    return httpResponseFuture;
  }

  private SourceQueue<Pair<HttpRequest, Promise<HttpResponse>>> sourceQueue() {
    Flow flow = http.superPool(materializer);
    return (SourceQueue<Pair<HttpRequest, Promise<HttpResponse>>>) Source.queue(bufferSize, OverflowStrategy.dropNew())
      .via(flow)
      .toMat(Sink.foreach((Pair<Try<HttpResponse>, Promise<HttpResponse>> p) -> p.second().complete(p.first())), Keep.left())
      .run(materializer);
  }

  public CompletionStage<Pair<StatusCode, String>> toStatusAndBody(HttpResponse httpResponse) {
    return httpResponse.entity().getDataBytes()
      .runFold("", (current, byteString) -> current + byteString.decodeString("UTF-8"), materializer)
      .thenApply(payload -> Pair.create(httpResponse.status(), payload));
  }

}
