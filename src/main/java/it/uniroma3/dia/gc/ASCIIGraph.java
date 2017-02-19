
package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.exception.ASCIIGraphFormatException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.util.Arrays;

/**
 * <p>This class implements a directed graph loaded from file.</p>
 *
 * <p>The first line of the file contains the number of nodes of the graph.
 * The next lines contain the list of successors of each node <code>n</code>;
 * the first element of the line is <code>n</code> itself. The first index
 * of nodes is 0. In the following there is an example of ASCII file of a
 * graph with 5 nodes.</p>
 *
 * <code>
 * 5<br>
 * 0 1 2<br>
 * 1 0<br>
 * 2<br>
 * 3 2 4<br>
 * 4 1 3<br>
 * </code>
 *
 * @author  Guido Drovandi
 * @version 0.2
 */
public final class ASCIIGraph implements Graph {

    private final String graphName;
    private final int N;
    private final long edges;
    private final Node[] nodes;
    private final boolean inLinks;
    private int[] indegrees;

    /**
     * Creates an <code>ASCIIGraph</code> from file.
     *
     * @param fileName the name of the file in which is stored the graph
     * @throws java.lang.Exception
     */
    public ASCIIGraph(final String fileName) throws Exception {
	this(fileName,false);
    }

    /**
     * Creates an <code>ASCIIGraph</code> from file.
     *
     * @param fileName the name of the file in which is stored the graph
     * @param inLinks  indicates if nodes should also know their predecessors
     * @throws java.lang.Exception
     */
    public ASCIIGraph(final String fileName, final boolean inLinks) throws Exception {
	int i,j;
	this.graphName=fileName;
	BufferedReader reader=new BufferedReader(new FileReader(fileName+".net"));
	N=Integer.parseInt(reader.readLine());
	if (N<=0) {
	    reader.close();
	    throw new ASCIIGraphFormatException("Network dimension "+N+" less than or euqal to 0!");
	}
	this.indegrees=null;
	this.inLinks=inLinks;
	long edges=0;
	this.nodes=new Node[N];
	for (i=0;i<N;i++) {
	    String s=reader.readLine();
	    if (s==null) break;
	    String[] tokens=s.split(" ");
	    final int degree=tokens.length-1;
	    int id=Integer.parseInt(tokens[0]);
	    if (id<0 || id>=N) {
		reader.close();
		throw new ASCIIGraphFormatException("Node out of bounds: node "+id+
						    " is greater than or equal to the network dimension "+N+"!");
	    }
	    if (this.nodes[id]!=null) {
		reader.close();
		throw new ASCIIGraphFormatException("Wrong network format: multiple "+id+"-node lines!");
	    }
	    Node node=new Node(id,degree);
	    this.nodes[id]=node;
	    for (j=1;j<tokens.length;j++) {
		edges++;
		id=Integer.parseInt(tokens[j]);
		if (id<0 || id>=N) {
		    reader.close();
		    throw new ASCIIGraphFormatException("Node out of bounds: node "+id+
							" is greater than or equal to the network dimension "+N+"!");
		}
		node.addOutLink(id);
	    }
	}
        this.edges = edges;
	if (!inLinks) {
	    for (i=0;i<N;i++) {
		if (this.nodes[i]==null) this.nodes[i]=new Node(i,0);
		else this.nodes[i].sort();
	    }
	} else {
	    for (i=0;i<N;i++) {
		if (this.nodes[i]==null) {
		    this.nodes[i]=new Node(i,0);
		} else {
		    final int[] out=nodes[i].getOutLinks();
		    for (j=0;j<out.length;j++) {
			if (nodes[out[j]]==null) nodes[out[j]]=new Node(i,0);
			nodes[out[j]].incInDegree();
		    }
		}
	    }
	    for (i=0;i<N;i++) {
		final int[] out=nodes[i].getOutLinks();
		for (j=0;j<out.length;j++)
		    nodes[out[j]].addInLink(i);
	    }
	    for (i=0;i<N;i++)
		this.nodes[i].sort();
	}

    }

    @Override
    public String getName() {
	return this.graphName;
    }

    @Override
    public int getVertexCount() {
	return this.N;
    }

    @Override
    public long getEdgeCount() {
	return this.edges;
    }

    @Override
    public int[] getSuccessors(final int node) {
	return this.nodes[node].getOutLinks();
    }

    /**
     * Returns an <code>int</code> array view of the predecessors of <code>node</code> in this graph.
     *
     * A vertex <code>v</code> is a predecessor of <code>node</code> if the edge <code>(v,node)</code> exists.
     *
     * @param  node the vertex whose predecessors are to be returned
     * @return      an <code>int</code> array view of the predecessors of <code>node</code> in this graph.
     *              <code>null</code> if nodes do not know their predecessors
     */
    @Override
    public int[] getPredecessors(final int node) {
	return inLinks?this.nodes[node].getInLinks():null;
    }

    @Override
    public int[] getNeighbourhood(final int node) {
	if (!inLinks) getSuccessors(node);
	int a,b,i;
	final int[] out=this.nodes[node].getOutLinks();
	final int[] in=this.nodes[node].getInLinks();
	final int[] neighbour=new int[out.length+in.length];
	a=b=0;
	for (i=0; a<out.length && b<in.length; i++) {
	    if (out[a]<in[b]) {
		neighbour[i]=out[a++];
	    } else if (out[a]>in[b]) {
		neighbour[i]=in[b++];
	    } else {
		neighbour[i]=out[a];
		a++;
		b++;
	    }
	}
	for (;a<out.length;a++)
	    neighbour[i++]=out[a++];
	for (;b<in.length;b++)
	    neighbour[i++]=in[b++];
	return Arrays.copyOfRange(neighbour,0,i);
    }

    @Override
    public int outDegree(final int node) {
	return this.nodes[node].outdegree();
    }

    @Override
    public int inDegree(final int node) {
	if (inLinks) {
	    return this.nodes[node].indegree();
	} else {
	    if (this.indegrees==null) this.indegrees=getInDegrees();
	    return this.indegrees[node];
	}
    }

    @Override
    public boolean isNeighbor(final int a,final int b) {
	return this.nodes[a].isOutNode(b);
    }

    @Override
    public int[] getOutDegrees() {
	int i;
	int[] degrees=new int[N];
	for (i=0;i<N;i++)
	    degrees[i]=nodes[i].outdegree();
	return degrees;
    }

    @Override
    public int[] getInDegrees() {
	int i;
	final int[] indegrees=new int[N];
	if (inLinks) {
	    for (i=0;i<N;i++)
		indegrees[i]=nodes[i].indegree();
	} else {
	    for (i=0;i<N;i++) {
		final int[] out=nodes[i].getOutLinks();
		for (int n:out)
		    indegrees[n]++;
	    }
	}
	return indegrees;
    }

}

