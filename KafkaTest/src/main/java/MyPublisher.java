import java.util.Properties;
import java.util.concurrent.*;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public class MyPublisher {

  public static final String BOOTSTRAP_SERVERS = "10.28.13.14:9583,10.28.12.62:9893,10.28.13.127:9180";

  public static void main(String[] args) throws ExecutionException, InterruptedException, TimeoutException {
    KafkaProducer<String, String> kafkaProducer = kafkaProducer(BOOTSTRAP_SERVERS);
    RecordMetadata rm = publish(kafkaProducer, "my-topic", "my-key", "my-message").toCompletableFuture()
        .get(5, TimeUnit.SECONDS);
    System.out.println("Partition: " + rm.partition() + " Offset: " + rm.offset() + " topic: " + rm.topic());
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
