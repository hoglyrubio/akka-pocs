package com.hogly.persistence;

import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.persistence.cassandra.query.javadsl.CassandraReadJournal;
import akka.persistence.query.EventEnvelope;
import akka.persistence.query.PersistenceQuery;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class App {

  public static void main(String[] args) {
    ActorSystem system = ActorSystem.create();
    ActorRef writer = system.actorOf(Props.create(Writer.class));
    writer.tell(new MyEvent(1, "one"), ActorRef.noSender());
    writer.tell(new MyEvent(2, "two"), ActorRef.noSender());

    CassandraReadJournal journal = PersistenceQuery.get(system).getReadJournalFor(CassandraReadJournal.class, CassandraReadJournal.Identifier());
    Source<EventEnvelope, NotUsed> source = journal.eventsByPersistenceId("persistence-id", 0L, Long.MAX_VALUE);
    source
      .map(EventEnvelope::event)
      .map(event -> {
        system.log().info("Read: {}", event);
        return (MyEvent) event;
      })
      .runWith(Sink.ignore(), ActorMaterializer.create(system));
  }

}
