package com.hogly;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.graph.GraphNode;
import com.datastax.driver.dse.graph.GraphResultSet;
import com.datastax.driver.dse.graph.GraphStatement;
import com.datastax.driver.dse.graph.SimpleGraphStatement;
import com.datastax.driver.dse.graph.Vertex;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class DseGraphTest {

  private static final String HOST = "127.0.0.1";
  private static final String GRAPH_NAME = "mygraph";

  private static DseCluster dseCluster;
  private static DseSession dseSession;

  @BeforeClass
  public static void beforeClass() {
    dseCluster = DseCluster.builder()
      .addContactPoint(HOST)
      .build();
    dseSession = dseCluster.connect();
    dseSession.executeGraph("system.graph('" + GRAPH_NAME + "').ifNotExists().create()");
  }

  @AfterClass
  public static void afterClass() {
    dseSession.close();
    dseCluster.close();
  }

  @Test
  public void shouldAddVertex() {
    // Create vertex
    GraphStatement addRepStatement = new SimpleGraphStatement("g.addV(label, 'REP')")
      .setGraphName(GRAPH_NAME)
      .setGraphWriteConsistencyLevel(ConsistencyLevel.QUORUM);
    dseSession.executeGraph(addRepStatement);

    // Count vertex
    GraphStatement countStatement = new SimpleGraphStatement("g.V().count()")
      .setGraphName(GRAPH_NAME)
      .setGraphWriteConsistencyLevel(ConsistencyLevel.QUORUM);

    GraphNode node = dseSession.executeGraph(countStatement).one();
    Assert.assertEquals(1, node.asInt());
  }

  @Test
  public void shouldReadAllVertex() {
    
  }

}
