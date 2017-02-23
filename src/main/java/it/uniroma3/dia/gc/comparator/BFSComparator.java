package it.uniroma3.dia.gc.comparator;

import it.uniroma3.dia.gc.BFSParser;
import it.uniroma3.dia.gc.Graph;
import java.util.Comparator;

/**
 * <p>This is the abstract class to implement a Comparator to perform BFS.</p>
 *
 * @author  Guido Drovandi
 * @version 0.3.5
 * @since   0.3.5
 */
public abstract class BFSComparator implements Comparator<Integer> {

    public Graph graph;
    public BFSParser parser;

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public void setParser(BFSParser parser) {
        this.parser = parser;
    }

    /**
     * This method is called immediately before to perform the sorting of the array
     * @param a      the array to be sorted
     * @param length the number of elements on which the sort will be performed. The array
     *               <code>a</code> can be larger than this value. Elements between <code>0</code>
     *               and <code>length - 1</code> (both inclusive) will be sorted
     */
    public abstract void beforeSort(Integer[] a, int length);

    /**
     * This method is called immediately after the array sorting
     * @param a      the array to be sorted
     * @param length the number of sorted elements
     */
    public abstract void afterSort(Integer[] a, int length);

    /**
     * This method is called when node <code>node</code> is added to the BFS queue
     * @param node the node added to the queue (new index)
     */
    public abstract void addToQueue(int node);

    /**
     * This method is called when the BFS is performed on this node
     * @param node the node on which the BFS is performed (new index)
     */
    public abstract void nodeBFS(int node);

    /**
     * Compare two nodes in order to sort the array.
     *
     * @param  node1 the first node to be compared
     * @param  node2 the second node to be compared
     * @return       the difference of the two outdegrees
     */
    @Override
    public abstract int compare(Integer node1, Integer node2);

}
