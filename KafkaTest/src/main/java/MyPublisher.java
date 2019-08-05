import java.util.Properties;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import akka.Done;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class MyPublisher {

  public static final String BOOTSTRAP_SERVERS = "localhost:9092";

  public static void main(String[] args) throws InterruptedException, ExecutionException, TimeoutException {
    ActorSystem system = ActorSystem.create();

    KafkaProducer<String, String> kafkaProducer = kafkaProducer(BOOTSTRAP_SERVERS);
    CompletionStage<Done> result = Source.range(4001, 5000)
      .mapAsync(10, i -> publish(kafkaProducer, "my-topic", "key-" + i, "message-" + i))
      .runForeach(recordMetadata -> system.log().info("{} {}", recordMetadata.partition(), recordMetadata.offset()), ActorMaterializer.create(system));

    result.toCompletableFuture().get(10, TimeUnit.SECONDS);
  }

  public static CompletionStage<RecordMetadata> publish(KafkaProducer<String, String> kafkaProducer, String topic, String key, String message) {
    ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, message);

    CompletableFuture<RecordMetadata> promise = new CompletableFuture<>();

    kafkaProducer.send(record, (recordMetadata, ex) -> {
      if (ex == null) {
        promise.complete(recordMetadata);
      } else {
        promise.completeExceptionally(ex);
      }
    });
    return promise;
  }

  public static KafkaProducer<String, String> kafkaProducer(String bootstrapServers) {
    Properties props = new Properties();
    props.put("bootstrap.servers", bootstrapServers);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    return new KafkaProducer<>(props);
  }

}
