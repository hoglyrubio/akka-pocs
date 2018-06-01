package com.hogly;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Session;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphNode;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.SimpleGraphStatement;
import com.datastax.driver.dse.graph.Vertex;
import com.google.common.util.concurrent.ListenableFuture;
import com.spotify.futures.CompletableFuturesExtra;

import java.util.concurrent.CompletableFuture;

public class TestingDseGraph {

  public static void main(String[] args) {
    DseCluster dseCluster = DseCluster.builder()
      .addContactPoint("127.0.0.1")
      .withGraphOptions(new GraphOptions().setGraphName("demo"))
      .build();
    DseSession dseSession = dseCluster.connect();
    createGraph(dseSession);
  }

  private static void usingAync(DseSession dseSession) {
    ListenableFuture<GraphResultSet> response = dseSession.executeGraphAsync("");
    CompletableFuture<GraphResultSet> f = CompletableFuturesExtra.toCompletableFuture(response);
  }

  private static void createGraph(DseSession dseSession) {
    //dseSession.executeGraph("system.graph('demo').ifNotExists().create()");
    GraphStatement s1 = new SimpleGraphStatement("g.addV(label, 'test_vertex')")
      .setGraphName("demo")
      .setGraphWriteConsistencyLevel(ConsistencyLevel.QUORUM);
    GraphResultSet graphResult = dseSession.executeGraph(s1);
    System.out.println("result: " + graphResult.all());
  }

  private static void queryGraph(DseSession dseSession) {
    GraphStatement s2 = new SimpleGraphStatement("g.V()").setGraphName("demo");
    GraphResultSet rs = dseSession.executeGraph(s2);
    GraphNode node = rs.one();
    Vertex vertex = node.asVertex();
    System.out.println(vertex);
  }

}
