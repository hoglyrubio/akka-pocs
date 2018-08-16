import akka.Done;
import akka.actor.ActorSystem;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.concurrent.CompletionStage;

public class MyConsumer {

  //public static final String BOOTSTRAP_SERVERS = "10.28.13.14:9583,10.28.12.62:9893,10.28.13.127:9180";
  public static final String BOOTSTRAP_SERVERS = "10.151.13.158:9990,10.151.15.94:9210,110.151.14.98:9826";

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create("kafka-testing");
    consumeAutoCommit(system, "my-topic", "my-consumer-group");
  }

  public static CompletionStage<Done> consumeAutoCommit(ActorSystem system, String topic, String consumerGroup) {

    ConsumerSettings<String, String> settings = ConsumerSettings.create(system, new StringDeserializer(), new StringDeserializer())
            .withBootstrapServers(BOOTSTRAP_SERVERS)
            .withGroupId(consumerGroup)
            .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")
            .withProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000")
            .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return Consumer.plainSource(settings, Subscriptions.topics(topic))
            .map(consumerRecord -> {
              system.log().info("Partition: {} Offset: {} Key: {} Value: {}", consumerRecord.partition(), consumerRecord.offset(), consumerRecord.key(), consumerRecord.value());
              return Done.getInstance();
            })
            .runWith(Sink.ignore(), ActorMaterializer.create(system));
  }

  public static CompletionStage<Done> consumeManualCommit(ActorSystem system, String topic, String consumerGroup) {

    ConsumerSettings<String, String> settings = ConsumerSettings.create(system, new StringDeserializer(), new StringDeserializer())
            .withBootstrapServers(BOOTSTRAP_SERVERS)
            .withGroupId(consumerGroup)
            .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
            .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

    return Consumer.committableSource(settings, Subscriptions.topics(topic))
            .mapAsync(1, commitableMessage -> {
              ConsumerRecord<String, String> record = commitableMessage.record();
              ConsumerMessage.PartitionOffset offset = commitableMessage.committableOffset().partitionOffset();
              system.log().info("Partition: {} Offset: {} Key: {} Value: {}", record.key(), record.value(), offset.key().partition(), offset.offset());
              return commitableMessage.committableOffset().commitJavadsl();
            })
            .runWith(Sink.ignore(), ActorMaterializer.create(system));
  }


}
