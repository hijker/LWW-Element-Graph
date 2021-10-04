# LWW-Graph
LWW-Graph is an implementation of Directed Last Write Wins Graph inspired from the paper,
[A comprehensive study of Convergent and Commutative Replicated Data Types]
(https://hal.inria.fr/inria-00555588/PDF/techreport.pdf)
By Marc Shapiro, Nuno Preguiça, Carlos Baquero, Marek Zawirsk

This implementation of LWW-Graph is a directed, state based [Conflict-free replicated data type (CvRDT)]
(https://en.wikipedia.org/wiki/Conflict-free_replicated_data_type)

Just like any other graph, LWW-Graph is also a collection of vertices and edges.
Each time a vertex (or edge) is added (or removed) the current time stamp
is also associated with the operation. This timestamp is later used to resolve conflicts while merging replicas.
The graph always maintains a collection for added vertices, added edges, removed vertices and removed edges.
The latest timestamp for each action is stored associated to the entry.
An edge can be added only if the associated vertices exist.
The implementation is biased towards vertices.
If you try to remove a vertex v which is part of an edge, all the edges from and to v will be removed.

Caution : Removing and adding a vertex back will not bring back the edges that got removed.

The graph can support any data type for vertex, provided it has a proper
hashCode() function defined which ensure no conflicts.

The merge algorithm is to first merge the added vertices collection then added edges collection
followed by removed edges collection and finally removed vertices collection.
The entry with the latest timestamp wins in case of conflict.

## Operations
The following operations are supported by LWW-Graph
1. Add/Remove vertex.
2. Add/Remove edge.
3. Check if a vertex exist in the graph.
4. Query all vertices connected to a vertex.
5. Find any path between two vertices.
6. Merge with concurrent changes from other graph/replica.

## Prerequisites
1. Java 8+
2. Gradle
3. TestNG

## Getting started
Open LWWGraph.java in the IDE to have a quick look at the implementation and functions.
To start using, create an instance of LWWGraph specifying the type and call necessary functions.

## Test coverage
The entire class is covered by unit tests. The unit test use Integer as the vertex data type.