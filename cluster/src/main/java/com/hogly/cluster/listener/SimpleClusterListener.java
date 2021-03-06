package com.hogly.cluster.listener;

import akka.actor.AbstractLoggingActor;
import akka.cluster.Cluster;
import akka.cluster.ClusterEvent;

public class SimpleClusterListener extends AbstractLoggingActor {

  Cluster cluster = Cluster.get(getContext().getSystem());

  @Override
  public void preStart() {
    log().info("PRE-START");
    cluster.subscribe(getSelf(), ClusterEvent.initialStateAsEvents(), ClusterEvent.MemberEvent.class, ClusterEvent.UnreachableMember.class);
  }

  @Override
  public void postStop() {
    log().info("POST-STOP");
    cluster.unsubscribe(getSelf());
  }

  @Override
  public Receive createReceive() {
    return receiveBuilder()
      .match(ClusterEvent.CurrentClusterState.class, msg -> log().info("Current members: {}", msg.members()))
      .match(ClusterEvent.MemberUp.class, msg -> log().info("Member is up: {}", msg.member()))
      .match(ClusterEvent.UnreachableMember.class, msg -> log().info("Member detected as unreachable: {}", msg.member()))
      .match(ClusterEvent.MemberRemoved.class, msg -> log().info("Member is removed: {}", msg.member()))
      .match(ClusterEvent.MemberEvent.class, msg -> log().info("Other event from member: {}", msg.member()))
      .matchAny(msg -> log().info("Unknown message: {}", msg))
      .build();
  }
}
