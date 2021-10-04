import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LWWGraphTest {

    private LWWGraph<Integer> integerLWWGraph;

    @BeforeMethod
    void setup() {
        integerLWWGraph = new LWWGraph<>();
    }

    /**
     * Tests add and remove vertex functionality and check if a vertex exist in the graph functionality
     */
    @Test
    public void testVertexExist() {
        integerLWWGraph.addVertex(1);
        //make sure vertex got added
        Assert.assertTrue(integerLWWGraph.isVertexPresent(1));
        integerLWWGraph.removeVertex(1);
        //make sure vertex got removed
        Assert.assertFalse(integerLWWGraph.isVertexPresent(1));
    }

    /**
     * Tests add and remove edge functionality
     */
    @Test
    public void testEdgeExpired() {
        integerLWWGraph.addVertex(1);
        integerLWWGraph.addVertex(2);
        //ensure vertex gets added
        Assert.assertTrue(integerLWWGraph.addEdge(1, 2));
        //ensure vertex is not expired
        Assert.assertFalse(integerLWWGraph.isEdgeExpired(1, 2, System.currentTimeMillis()));
        //ensure vertex gets removed
        Assert.assertTrue(integerLWWGraph.removeEdge(1, 2));
        //ensure vertex is expired
        Assert.assertTrue(integerLWWGraph.isEdgeExpired(1, 2, System.currentTimeMillis()));
    }

    /**
     * Tests get connected vertices functionality
     */
    @Test
    public void testConnectedVertices() {
        integerLWWGraph.addVertex(0);
        integerLWWGraph.addVertex(1);
        integerLWWGraph.addVertex(2);
        integerLWWGraph.addVertex(3);
        integerLWWGraph.addEdge(0, 1);
        integerLWWGraph.addEdge(0, 2);
        integerLWWGraph.addEdge(1, 2);
        integerLWWGraph.addEdge(2, 0);
        integerLWWGraph.addEdge(2, 3);
        integerLWWGraph.addEdge(3, 3);

        final List<Integer> connectedVertices = integerLWWGraph.getConnectedVertices(2);
        Assert.assertEquals(connectedVertices, Arrays.asList(0, 3));
    }

    /**
     * Test find path functionality
     * Data taken from : https://www.geeksforgeeks.org/find-if-there-is-a-path-between-two-vertices-in-a-given-graph/
     */
    @Test
    public void testFindPath() {
        integerLWWGraph.addVertex(0);
        integerLWWGraph.addVertex(1);
        integerLWWGraph.addVertex(2);
        integerLWWGraph.addVertex(3);
        integerLWWGraph.addEdge(0, 1);
        integerLWWGraph.addEdge(0, 2);
        integerLWWGraph.addEdge(1, 2);
        integerLWWGraph.addEdge(2, 0);
        integerLWWGraph.addEdge(2, 3);
        integerLWWGraph.addEdge(3, 3);

        final List<Integer> path = integerLWWGraph.findPath(1, 3);
        Assert.assertEquals(path, Arrays.asList(1, 2, 3));

    }

    /**
     * Test for non-existing paths
     */
    @Test
    public void testFindPathEmpty() {
        integerLWWGraph.addVertex(0);
        integerLWWGraph.addVertex(1);
        integerLWWGraph.addVertex(2);
        integerLWWGraph.addVertex(3);
        integerLWWGraph.addEdge(0, 1);
        integerLWWGraph.addEdge(0, 2);
        integerLWWGraph.addEdge(1, 2);
        integerLWWGraph.addEdge(2, 0);
        integerLWWGraph.addEdge(2, 3);
        integerLWWGraph.addEdge(3, 3);

        Assert.assertTrue(integerLWWGraph.findPath(3, 1).isEmpty());
    }

    /**
     * Test merge functionality
     */
    @Test
    public void testMergeAnotherGraph() {
        final LWWGraph<Integer> other = new LWWGraph<>();
        other.addVertex(0);
        other.addVertex(1);
        other.addVertex(2);
        other.addVertex(3);
        other.addEdge(0, 1);
        other.addEdge(0, 2);
        other.addEdge(1, 2);
        other.addEdge(2, 0);
        other.addEdge(2, 3);
        other.addEdge(3, 3);
        other.addEdge(3, 4);

        final LWWGraph<Integer> mergedIntegerLWWGraph = integerLWWGraph.mergeAnotherGraph(other);

        Assert.assertEquals(mergedIntegerLWWGraph.graphAsString(),
                "The vertices are : 0, 1, 2, 3\nThe edges are : 0 -> 1, 0 -> 2, 1 -> 2, 2 -> 0, 2 -> 3, 3 -> 3");
    }

    /**
     * Test basic graph creation
     */
    @Test
    public void testGraphCreation() {
        //ensure graph is empty initially
        Assert.assertEquals(integerLWWGraph.graphAsString(), "No vertices exist on the graph\nNo edges exist on the graph");

        integerLWWGraph.addVertex(0);
        integerLWWGraph.addVertex(1);
        integerLWWGraph.addVertex(2);
        integerLWWGraph.addVertex(3);
        integerLWWGraph.addEdge(0, 1);
        integerLWWGraph.addEdge(0, 2);
        integerLWWGraph.addEdge(1, 2);
        integerLWWGraph.addEdge(2, 0);
        integerLWWGraph.addEdge(2, 3);
        integerLWWGraph.addEdge(3, 3);
        integerLWWGraph.addEdge(3, 4);
        //Ensure graph got created as expected
        Assert.assertEquals(integerLWWGraph.graphAsString(),
                "The vertices are : 0, 1, 2, 3\nThe edges are : 0 -> 1, 0 -> 2, 1 -> 2, 2 -> 0, 2 -> 3, 3 -> 3");

        integerLWWGraph.addVertex(4);
        integerLWWGraph.addEdge(3, 4);
        integerLWWGraph.removeEdge(5, 6);   //ensure dummy steps has no effect
        Assert.assertEquals(integerLWWGraph.graphAsString(), "The vertices are : 0, 1, 2, 3, 4\n" +
                "The edges are : 0 -> 1, 0 -> 2, 1 -> 2, 2 -> 0, 2 -> 3, 3 -> 3, 3 -> 4");

        integerLWWGraph.removeVertex(4);    //This will also remove edge 3 -> 4
        Assert.assertEquals(integerLWWGraph.graphAsString(),
                "The vertices are : 0, 1, 2, 3\nThe edges are : 0 -> 1, 0 -> 2, 1 -> 2, 2 -> 0, 2 -> 3, 3 -> 3");
    }

    /**
     * Cover edge cases
     */
    @Test
    public void testFindPathCornerCases() {
        Assert.assertTrue(integerLWWGraph.findPath(0, 1).isEmpty());

        integerLWWGraph.addVertex(0);
        Assert.assertEquals(integerLWWGraph.findPath(0, 0), Collections.singletonList(0));
    }

    /**
     * Test to make sure deep copy gives a different object from the actual.
     */
    @Test
    public void testDeepCopy() {
        final LWWGraph<Integer> integerLWWGraphCopy = this.integerLWWGraph.deepCopy();
        Assert.assertNotEquals(integerLWWGraph, integerLWWGraphCopy);
    }

    /**
     * Test properties of state based merge function, i.e; commutative, associative, and idempotent
     * Here 3 {@link LWWGraph}s are created and then each property of merge is asserted.
     * <p>
     * In graph1, 4 vertices (1, 2, 3, 4) and 4 edges (1-> 2, 2 -> 3, 1 -> 3, 3 -> 4) are added. Then edge 1 -> 3 is deleted.
     * In graph2, 3 vertices (1, 3, 4) and 2 edges (1-> 3, 3 -> 4) are added.
     * In graph3, 3 vertices (2, 3, 4) and 1 edge (3 -> 2) are added. Then vertex 4 is deleted (which will drop edges from 4).
     * <p>
     * The test asserts the string form the graphs created by merging are equal.
     */
    @Test
    public void mergePropertiesTest() {
        final LWWGraph<Integer> graph1 = new LWWGraph<>();
        graph1.addVertex(1);
        graph1.addVertex(2);
        graph1.addVertex(3);
        graph1.addVertex(4);
        graph1.addEdge(1, 2);
        graph1.addEdge(2, 3);
        graph1.addEdge(1, 3);
        graph1.addEdge(3, 4);
        graph1.removeEdge(1, 3);

        final LWWGraph<Integer> graph2 = new LWWGraph<>();
        graph2.addVertex(1);
        graph2.addVertex(3);
        graph2.addVertex(4);
        graph2.addEdge(1, 3);
        graph2.addEdge(3, 4);

        final LWWGraph<Integer> graph3 = new LWWGraph<>();
        graph3.addVertex(2);
        graph3.addVertex(3);
        graph3.addVertex(4);
        graph3.addEdge(3, 2);
        graph3.removeVertex(4);

        final LWWGraph<Integer> graph1MergeGraph2 = graph1.mergeAnotherGraph(graph2);
        final LWWGraph<Integer> graph2MergeGraph1 = graph2.mergeAnotherGraph(graph1);
        //Assert commutativity
        Assert.assertEquals(graph1MergeGraph2.graphAsString(), graph2MergeGraph1.graphAsString());

        final LWWGraph<Integer> graph1MergeGraph2ThenMergeGraph3 = graph1MergeGraph2.mergeAnotherGraph(graph3);
        final LWWGraph<Integer> graph2MergeGraph3 = graph2.mergeAnotherGraph(graph3);
        final LWWGraph<Integer> graph1ThenMergeGraph2MergeGraph3 = graph1.mergeAnotherGraph(graph2MergeGraph3);
        //Assert associativity
        Assert.assertEquals(graph1MergeGraph2ThenMergeGraph3.graphAsString(), graph1ThenMergeGraph2MergeGraph3.graphAsString());

        final LWWGraph<Integer> graph1MergeGraph2MergeGraph2 = graph1MergeGraph2.mergeAnotherGraph(graph2);
        //Assert idempotence
        Assert.assertEquals(graph1MergeGraph2MergeGraph2.graphAsString(), graph1MergeGraph2.graphAsString());
    }

    /**
     * Test replica operations and merge, proof for conflict resolution
     * <p>
     * Here a {@link LWWGraph} called source graph is created and then replicated to another different graphs called replica.
     * Operations are carried out on both graphs separately, and finally they are merged.
     * Merging in both ways results in the same final state.
     */
    @Test
    public void conflictFreeReplicaTest() {
        final LWWGraph<Integer> sourceGraph = new LWWGraph<>();
        sourceGraph.addVertex(1);
        sourceGraph.addVertex(2);
        sourceGraph.addVertex(3);
        sourceGraph.addVertex(4);
        sourceGraph.addEdge(1, 2);
        sourceGraph.addEdge(2, 3);
        sourceGraph.addEdge(1, 3);
        sourceGraph.addEdge(3, 4);
        sourceGraph.removeEdge(1, 3);
        sourceGraph.addVertex(4);
        sourceGraph.addVertex(5);
        sourceGraph.addEdge(3, 4);
        sourceGraph.addEdge(4, 4);
        sourceGraph.removeEdge(4, 1);

        final LWWGraph<Integer> replica = sourceGraph.deepCopy();
        //Assert that source and replica are in same state initially
        Assert.assertEquals(sourceGraph.graphAsString(), replica.graphAsString());

        replica.removeVertex(3);
        sourceGraph.addVertex(3);
        replica.addEdge(4, 5);
        sourceGraph.addVertex(6);
        sourceGraph.addVertex(7);
        sourceGraph.addVertex(8);
        replica.addVertex(8);
        replica.removeVertex(2);
        sourceGraph.addEdge(7, 8);

        //Assert that source and replica are in different states currently
        Assert.assertNotEquals(sourceGraph.graphAsString(), replica.graphAsString());
        //Assert merging any one graph to other in any order results in same final state
        Assert.assertEquals(sourceGraph.mergeAnotherGraph(replica).graphAsString(),
                replica.mergeAnotherGraph(sourceGraph).graphAsString());
    }
}