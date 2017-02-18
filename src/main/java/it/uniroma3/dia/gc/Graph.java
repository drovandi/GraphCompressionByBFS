
package it.uniroma3.dia.gc;

/**
 * This interface permits to define a directed graph represented as adjacency lists.
 *
 * @author  Guido Drovandi
 * @version 0.1.1
 */
public interface Graph {

    /**
     * Returns the name of the graph.
     *
     * @return the name of the graph
     */
    String getName();

    /**
     * Returns the number of vertices in this graph.
     *
     * @return the number of vertices
     */
    int getVertexCount();

    /**
     * Returns the number of edges in this graph.
     *
     * @return the number of edges
     */
    long getEdgeCount();

    /**
     * Returns an <code>int</code> array view of the predecessors of <code>node</code> in this graph.
     *
     * A vertex <code>v</code> is a predecessor of <code>node</code> if the edge <code>(v,node)</code> exists.
     *
     * @param  node the vertex whose predecessors are to be returned
     * @return      an <code>int</code> array view of the predecessors of <code>node</code> in this graph
     */
    int[] getPredecessors(int node);

    /**
     * Returns an <code>int</code> array view of the successors of <code>node</code> in this graph.
     *
     * A vertex <code>v</code> is a successor of <code>node</code> if the edge <code>(node,v)</code> exists.
     *
     * @param  node the vertex whose successors are to be returned
     * @return      an <code>int</code> array view of the successors of <code>node</code> in this graph
     */
    int[] getSuccessors(int node);

    /**
     * Returns an <code>int</code> array view of the successors and predecessors of <code>node</code> in this graph.
     * A neighbour that is bith successor and predecessor appears only once.
     *
     * @param  node the vertex whose successors and predecessors are to be returned
     * @return      an <code>int</code> array view of the successors and predecessors of <code>node</code> in this graph
     */
    int[] getNeighbourhood(int node);

    /**
     * Returns the number of successors (outgoing edges) of <code>node</code>.
     *
     * @param  node the vertex whose outdegree is to be returned
     * @return      the number of successors of <code>node</code>
     */
    int outDegree(int node);

    /**
     * Returns the number of predecessors (ingoing edges) of <code>node</code>.
     *
     * @param  node the vertex whose indegree is to be returned
     * @return      the number of predecessors of <code>node</code>
     */
    int inDegree(int node);

    /**
     * Returns an <code>int</code> array view of outdegrees in this graph.
     *
     * @return      an <code>int</code> array view of outdegrees
     */
    int[] getOutDegrees();

    /**
     * Returns an <code>int</code> array view of indegrees in this graph.
     *
     * @return      an <code>int</code> array view of indegrees
     */
    int[] getInDegrees();

    /**
     * Check if the edge <code>(a,b)</code> exists.
     *
     * @param a the source of the edge
     * @param b the destination of the edge
     * @return  returns <code>true</code> if <code>(a,b)</code> is an edge
     */
    boolean isNeighbor(int a,int b);

}

