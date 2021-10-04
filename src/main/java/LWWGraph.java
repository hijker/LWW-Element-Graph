import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of a state based Last write wins directed graph.
 * The graph is biased toward vertex, that is it removes associated edges when a vertex gets removed.
 * <p>
 * The class is made serializable to support deep copy to create replicas
 * <p>
 * verticesAdded stores vertices that were ever added with the associated time stamp.
 * verticesRemoved stores vertices that were ever removed with the associated time stamp.
 * edgesAdded stores edges connecting valid vertices that were added with the associated time stamp.
 * edgesRemoved stores edges connecting valid vertices that were removed with the associated time stamp.
 *
 * @param <V> Type of vertex.
 */
public class LWWGraph<V> implements Serializable {

    private final Map<V, Long> verticesAdded;
    private final Map<V, Long> verticesRemoved;
    private final Map<V, Map<V, Long>> edgesAdded;
    private final Map<V, Map<V, Long>> edgesRemoved;

    /**
     * Constructor
     */
    public LWWGraph() {
        this.verticesAdded = new HashMap<>();
        this.verticesRemoved = new HashMap<>();
        this.edgesAdded = new HashMap<>();
        this.edgesRemoved = new HashMap<>();
    }

    /************ getters ***************/

    public Map<V, Long> getVerticesAdded() {
        return verticesAdded;
    }

    public Map<V, Long> getVerticesRemoved() {
        return verticesRemoved;
    }

    public Map<V, Map<V, Long>> getEdgesAdded() {
        return edgesAdded;
    }

    public Map<V, Map<V, Long>> getEdgesRemoved() {
        return edgesRemoved;
    }

    /**
     * Function to add a vertex.
     *
     * @param v vertex to be added of type {@link V}
     */
    public void addVertex(final V v) {
        verticesAdded.put(v, System.currentTimeMillis());
    }

    /**
     * Function to remove a vertex.
     *
     * @param v vertex to be added of type {@link V}
     */
    public void removeVertex(final V v) {
        verticesRemoved.put(v, System.currentTimeMillis());
        edgesAdded.remove(v);
        edgesRemoved.remove(v);
        edgesAdded.values().forEach(connection -> connection.remove(v));
        edgesRemoved.values().forEach(connection -> connection.remove(v));
    }

    /**
     * Function to add an edge. Returns true if edge was added successfully, else false.
     *
     * @param from vertex from which the edge start.
     * @param to   vertex to which the edge is.
     * @return Boolean representing successful creation of edge.
     */
    public boolean addEdge(final V from,
                           final V to) {
        if (!isVertexPresent(from) || !isVertexPresent(to)) {
            return false;
        }
        final Map<V, Long> existingEdges = edgesAdded.getOrDefault(from, new HashMap<>());
        existingEdges.put(to, System.currentTimeMillis());
        edgesAdded.put(from, existingEdges);
        return true;
    }

    /**
     * Function to remove an edge. Returns true if edge was removed successfully, else false.
     *
     * @param from vertex from which the edge start.
     * @param to   vertex to which the edge is.
     * @return Boolean representing successful deletion of edge.
     */
    public boolean removeEdge(final V from,
                              final V to) {
        if (!isVertexPresent(from) || !isVertexPresent(to)) {
            return false;
        }
        final Map<V, Long> existingEdges = edgesRemoved.getOrDefault(from, new HashMap<>());
        existingEdges.put(to, System.currentTimeMillis());
        edgesRemoved.put(from, existingEdges);
        return true;
    }

    /**
     * Checks for presence of a vertex in the graph.
     *
     * @param vertex the vertex to be checked for presence.
     * @return Boolean representing the presence of the vertex.
     */
    public boolean isVertexPresent(final V vertex) {
        return verticesAdded.containsKey(vertex)
                && verticesAdded.get(vertex) > verticesRemoved.getOrDefault(vertex, Long.MIN_VALUE);
    }

    /**
     * Returns a list of vertices connected to a vertex.
     * Returns empty list when the vertex is absent or when no other vertex is connected to the given vertex.
     *
     * @param vertex vertex to get the connections for.
     * @return {@link List} of vertices connected to the given vertex.
     */
    public List<V> getConnectedVertices(final V vertex) {
        final List<V> connectedVertices = new ArrayList<>();
        edgesAdded.getOrDefault(vertex, new HashMap<>()).forEach((toVertex, timeStamp) -> {
            if (isEdgeExpired(vertex, toVertex, timeStamp)) {
                return;
            }
            connectedVertices.add(toVertex);
        });
        return connectedVertices;
    }

    /**
     * Returns the list of vertices in the shortest path between the given vertices.
     * Returns empty list if the vertices does not exist or when the path does not exist.
     *
     * @param from vertex from which the path start.
     * @param to   vertex to which the path is.
     * @return {@link List} of vertices in the path.
     */
    public List<V> findPath(final V from,
                            final V to) {
        //Case when the vertices do not exist.
        if (!isVertexPresent(from) || !isVertexPresent(to)) {
            return Collections.emptyList();
        }

        //Case when start and end are same
        if (from.equals(to)) {
            return Collections.singletonList(from);
        }
        final List<V> path = new ArrayList<>();
        final Set<V> visited = new HashSet<>();
        doDFS(visited, from, path, to);
        return path;
    }

    /**
     * Function to merge two {@link LWWGraph}. It accepts the second graph as a param and returns the result of merging
     * current graph with the second graph. Assign the result to the original object when called to reflect the changes
     * on same object.
     *
     * @param other the {@link LWWGraph} to be merged with the current graph.
     * @return a new {@link LWWGraph} created as the result of merging.
     */
    public LWWGraph<V> mergeAnotherGraph(final LWWGraph<V> other) {
        final LWWGraph<V> merged = deepCopy();

        //add vertices
        other.getVerticesAdded().forEach((vertex, timeStamp) -> merged.getVerticesAdded()
                .put(vertex, Math.max(merged.getVerticesAdded().getOrDefault(vertex, Long.MIN_VALUE), timeStamp)));

        //add edges
        final Map<V, Map<V, Long>> edgesAdded = merged.getEdgesAdded();
        other.getEdgesAdded().forEach((fromVertex, connections) -> {
            final Map<V, Long> fullConnections = edgesAdded.getOrDefault(fromVertex, new HashMap<>());
            connections.forEach((toVertex, timeStamp) -> fullConnections
                    .put(toVertex, Math.max(fullConnections.getOrDefault(toVertex, Long.MIN_VALUE), timeStamp)));
            edgesAdded.put(fromVertex, fullConnections);
        });

        //remove edges
        final Map<V, Map<V, Long>> edgesRemoved = merged.getEdgesRemoved();
        other.getEdgesRemoved().forEach((fromVertex, connections) -> {
            final Map<V, Long> fullConnections = edgesRemoved.getOrDefault(fromVertex, new HashMap<>());
            connections.forEach((toVertex, timeStamp) -> fullConnections
                    .put(toVertex, Math.max(fullConnections.getOrDefault(toVertex, Long.MIN_VALUE), timeStamp)));
            edgesRemoved.put(fromVertex, fullConnections);
        });

        //remove vertices
        other.getVerticesRemoved().forEach((vertex, timeStamp) -> merged.getVerticesRemoved()
                .put(vertex, Math.max(merged.getVerticesRemoved().getOrDefault(vertex, Long.MIN_VALUE), timeStamp)));

        return merged;
    }

    /**
     * Helper method to make a deep copy
     * Taken from : https://www.journaldev.com/17129
     */
    public LWWGraph<V> deepCopy() {
        LWWGraph<V> copy = null;
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(this);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            copy = (LWWGraph<V>) objInputStream.readObject();
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return copy;
    }

    /**
     * Helper method
     * Depth first search function to help the find path function.
     *
     * @param visited       {@link Set} storing visited vertices.
     * @param currentVertex vertex currently being looked at.
     * @param path          {@link List} storing vertices in path currently.
     * @param to            destination vertex.
     * @return Boolean representing if the destination was found or not.
     */
    private boolean doDFS(final Set<V> visited,
                          final V currentVertex,
                          final List<V> path,
                          final V to) {
        path.add(currentVertex);
        visited.add(currentVertex);
        if (currentVertex.equals(to)) {
            return true;
        }
        final List<V> connectedVertices = getConnectedVertices(currentVertex);
        for (final V v : connectedVertices) {
            if (!visited.contains(v)) {
                if (doDFS(visited, v, path, to)) {
                    return true;
                }
            }
        }
        path.remove(path.size() - 1);
        return false;
    }

    /**
     * Helper method
     * Check is an edge is valid based on the timestamp and the time stamps in the Maps.
     *
     * @param from      vertex from which the edge start.
     * @param to        vertex to which the edge is.
     * @param timeStamp time stamp to be compared with.
     * @return Boolean representing the expiry of edge.
     */
    boolean isEdgeExpired(final V from,
                          final V to,
                          final Long timeStamp) {
        return edgesRemoved.containsKey(from) && edgesRemoved.get(from).getOrDefault(to, Long.MIN_VALUE) <= timeStamp;
    }

    /**
     * Helper method
     * Function to get the graph as a formatted string to help in visualising and testing.
     *
     * @return Formatted string representing the graph.
     */
    String graphAsString() {
        final StringBuilder graphStringBuilder = new StringBuilder();
        final List<String> validVertices = verticesAdded.keySet().stream()
                .filter(this::isVertexPresent)
                .map(V::toString)
                .collect(Collectors.toList());
        if (validVertices.isEmpty()) {
            graphStringBuilder.append("No vertices exist on the graph");
        } else {
            graphStringBuilder.append("The vertices are : ")
                    .append(String.join(", ", validVertices));
        }
        graphStringBuilder.append("\n");
        final List<String> validEdges = new ArrayList<>();
        edgesAdded.forEach((fromVertex, connections) -> connections.forEach((toVertex, timeStamp) -> {
            if (!isEdgeExpired(fromVertex, toVertex, timeStamp)) {
                validEdges.add(fromVertex + " -> " + toVertex);
            }
        }));
        if (validEdges.isEmpty()) {
            graphStringBuilder.append("No edges exist on the graph");
        } else {
            graphStringBuilder.append("The edges are : ")
                    .append(String.join(", ", validEdges));
        }
        return graphStringBuilder.toString();
    }
}