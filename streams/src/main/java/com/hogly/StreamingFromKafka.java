package com.hogly;

import akka.Done;
import akka.actor.AbstractLoggingActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.kafka.ConsumerMessage;
import akka.kafka.ConsumerSettings;
import akka.kafka.Subscriptions;
import akka.kafka.javadsl.Consumer;
import akka.pattern.PatternsCS;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.concurrent.CompletionStage;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamingFromKafka extends AbstractLoggingActor {

  public static void main(String[] args) {
    Logger.getGlobal().setLevel(Level.INFO);
    ActorSystem system = ActorSystem.create();
    ActorRef actor = system.actorOf(Props.create(StreamingFromKafka.class, () -> new StreamingFromKafka()));
    actor.tell("start", ActorRef.noSender());
  }

  private static final String BOOTSTRAP_SERVERS = "localhost:9092";
  private static final String CONSUMER_GROUP = "test1";
  private static final String TOPIC = "my-topic";

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .matchEquals("start", msg -> start())
      .match(Status.Failure.class, this::handleFailure)
      .build();
  }

  private void handleFailure(Status.Failure failure) {
    throw new RuntimeException("Received failure", failure.cause());
  }

  private ConsumerSettings<String, String> consumerSettings() {
    return ConsumerSettings.create(context().system(), new StringDeserializer(), new StringDeserializer())
      .withBootstrapServers(BOOTSTRAP_SERVERS)
      .withGroupId(CONSUMER_GROUP)
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
  }

  private void start() {
    CompletionStage<Done> result = Consumer.committableSource(consumerSettings(), Subscriptions.topics(TOPIC))
      .mapAsync(10, commitableMessage -> {
        ConsumerRecord<String, String> record = commitableMessage.record();
        ConsumerMessage.PartitionOffset offset = commitableMessage.committableOffset().partitionOffset();
        log().info("Partition: {} Offset: {} Key: {} Value: {}", record.key(), record.value(), offset.key().partition(), offset.offset());
        return commitableMessage.committableOffset().commitJavadsl();
      })
      .runWith(Sink.ignore(), ActorMaterializer.create(context()));

    PatternsCS.pipe(result, context().dispatcher()).to(self());
  }

}