package com.hogly.dse.graph;

import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphOptions;
import com.datastax.dse.graph.api.DseGraph;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasicGraphTest {

  private static DseCluster dseCluster;
  private static DseSession dseSession;

  @BeforeClass
  public static void beforeClass() {
    dseCluster = DseCluster.builder()
            .addContactPoint("localhost")
            //.withGraphOptions(new GraphOptions().setGraphName("test"))
            .build();
    dseSession = dseCluster.connect();
    dseSession.executeGraph("system.graph('test').ifNotExists().create()");
  }

  @AfterClass
  public static void afterClass() {
    dseSession.close();
    dseCluster.close();
  }

  @Test
  public void createNodes() {
    GraphTraversalSource g = DseGraph.traversal(dseSession);
    GraphTraversal<Edge, Edge> person1 = g.addE("PERSON");
    GraphTraversal<Edge, Edge> person2 = g.addE("PERSON");
  }

}
