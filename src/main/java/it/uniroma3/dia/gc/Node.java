
package it.uniroma3.dia.gc;

import java.util.Arrays;

/**
 * <p>This class describe a node of a graph.</p>
 *
 * <p>This implementation presents some minor tricks to better use the memory.
 * This is used by <code>ASCIIGraph</code>, do not use standalone.</p>
 *
 * @author  Guido Drovandi
 * @version 0.2
 */
final class Node {

    private final int id;
    private int indegree,counter;
    private int[] outlinks;
    private int[] inlinks;

    /**
     * Create a node with the specified id.
     *
     * @param id the id of this node
     */
    protected Node(final int id) {
	this.counter=0;
	this.id=id;
	this.indegree=0;
	this.inlinks=null;
	this.outlinks=null;
    }

    /**
     * Create a node with the specified id and a fixed number of successors.
     *
     * @param id the id of this node
     * @param outdegree the number of successors
     */
    Node(final int id, final int outdegree) {
	this(id);
	setOutDegree(outdegree);
    }

    /**
     * Sort the sets of successors and predecessors.
     */
    void sort() {
	if (this.outlinks!=null) Arrays.sort(this.outlinks);
	if (this.inlinks!=null) Arrays.sort(this.inlinks);
    }

    /**
     * Returns the id of this node.
     * 
     * @return
     */
    int getId() {
	return this.id;
    }

    /**
     * Set the number of successors.
     *
     * @param outdegree the number of successors
     */
    void setOutDegree(final int outdegree) {
	this.outlinks=new int[outdegree];
    }

    /**
     * Set the number of predecessors.
     *
     * @param indegree the number of predecessors
     */
    void setInDegree(final int indegree) {
	this.indegree=indegree;
	this.inlinks=new int[indegree];
    }

    /**
     * Set <code>node</code> as a successor.
     *
     * @param node the successor to be added
     */
    void addOutLink(final int node) {
	this.outlinks[counter++]=node;
    }

    /**
     * Set <code>node</code> as a predecessor.
     *
     * @param node the predecessor to be added
     */
    void addInLink(final int node) {
	if (this.inlinks==null) {
	    setInDegree(indegree);
	    counter=0;
	}
	this.inlinks[counter++]=node;
    }

    /**
     * Check if <code>node</code> is a successor.
     *
     * @param node the successor to be checked
     * @return     <code>true</code> if <code>node</code> is a successor
     */
    boolean isOutNode(final int node) {
	return Arrays.binarySearch(this.outlinks,node)>=0;
    }

    /**
     * Returns an array of the successors.
     *
     * @return an <code>int</code> array of the successors
     */
    int[] getOutLinks() {
	return this.outlinks;
    }

    /**
     * Returns an array of the predecessors.
     *
     * @return an <code>int</code> array of the predecessors
     */
    int[] getInLinks() {
	return this.inlinks;
    }

    /**
     * Returns the number of successors (outdegree).
     *
     * @return the number of successors
     */
    int outdegree() {
	if (this.outlinks==null) return 0;
	return this.outlinks.length;
    }

    /**
     * Returns the number of predecessors (indegree).
     *
     * @return the number of predecessors
     */
    int indegree() {
	if (this.inlinks==null) return 0;
	return this.indegree;
    }

    /**
     * Increments the number of predecessors of this node.
     */
    void incInDegree() {
	this.indegree++;
    }

}

