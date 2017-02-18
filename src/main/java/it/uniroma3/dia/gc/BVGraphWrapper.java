
package it.uniroma3.dia.gc;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import java.io.PrintStream;

/**
 * <p>This is a wrapper class for <code>BVGraph</code> from the framework <code>WebGraph</code>
 * (<code>http://webgraph.dsi.unimi.it/</code>).</p>
 *
 * <p>This class allows to use graphs compressed using the technique by Boldi and Vigna. Their datasets
 * and framework are available at <code>http://webgraph.dsi.unimi.it/</code></p>
 *
 * <dl>
 * <dt><b>See Also:</b></dt>
 * <dd><code>http://webgraph.dsi.unimi.it/</code></dd>
 * <dd>"The WebGraph Framework I: Compression Techniques" by Paolo Boldi and Sebastiano Vigna, 2004</dd>
 * </dl>
 *
 * @author  Guido Drovandi
 * @version 0.1.1
 */
public class BVGraphWrapper implements Graph {

    private final BVGraph graph;
    private final String graphName;
    private int[] indegrees;

    public static void main(String[] args) throws Exception {
        long t1,t2;

        t1=System.currentTimeMillis();
        BVGraphWrapper bv=new BVGraphWrapper(args[0]);
        t2=System.currentTimeMillis();
        System.out.printf("Time: %d ms\n",t2-t1);

	bv.test(20000000,System.out);
    }

    private void test(final int test, final PrintStream out) {
        int i,N;
        long t1,t2;
	java.util.Random r=new java.util.Random();
	N=this.graph.numNodes();
        t1=System.currentTimeMillis();
        for (i=0;i<test;i++) {
	    LazyIntIterator it=this.graph.successors(r.nextInt(N));
	    while (it.nextInt()!=-1);
	}
        t2=System.currentTimeMillis();
        out.printf("Average: %3.3f us\n",(1.*(t2-t1)/test)*1000);
    }

    /**
     * Creates a <code>BVGraphWrapper</code> from file.
     *
     * @param fileName the file in which is stored the graph
     * @throws java.lang.Exception
     */
    public BVGraphWrapper(final String fileName) throws Exception {
	this.graphName=fileName;
	this.graph=BVGraph.load(fileName);
    }

    /**
     * Returns the <code>BVGraph</code> associated to this wrapper.
     *
     * @return the <code>BVGraph</code> associated to this wrapper
     */
    public final BVGraph getBVGraph() {
	return this.graph;
    }

    @Override
    public final String getName() {
	return this.graphName;
    }

    @Override
    public final int getVertexCount() {
	return this.graph.numNodes();
    }

    @Override
    public final long getEdgeCount() {
	return this.graph.numArcs();
    }

    @Override
    public final int outDegree(final int node) {
	return this.graph.outdegree(node);
    }

//     public final int inDegree(final int node) {
// 	return 0;
//     }
    @Override
    public final int inDegree(final int node) {
	if (this.indegrees==null) this.indegrees=getInDegrees();
	return this.indegrees[node];
    }

    @Override
    public final boolean isNeighbor(final int a, final int b) {
	int n;
	LazyIntIterator neighbours=this.graph.successors(a);
	while ( (n=neighbours.nextInt())!=-1 ) {
	    if (n==b) return true;
	}
	return false;
    }

    @Override
    public final int[] getSuccessors(final int node) {
	int i;
	int[] neighbours=new int[outDegree(node)];
	LazyIntIterator it=this.graph.successors(node);
	for (i=0;i<neighbours.length;i++)
	    neighbours[i]=it.nextInt();
	return neighbours;
    }

    @Override
    public final int[] getPredecessors(final int node) {
	return null;
    }

    @Override
    public int[] getNeighbourhood(final int node) {
	return null;
    }

    /**
     * Write the graph to ASCII format on the specified <code>PrintStream</code>.
     *
     * @param out the <code>PrintStream</code> where to write
     */
    public final void printASCIIGraph(final PrintStream out) {
	int i,n;
	out.println(getVertexCount());
	for (i=0;i<getVertexCount();i++) {
	    LazyIntIterator it=this.graph.successors(i);
	    out.print(i);
	    while ( (n=it.nextInt())!=-1 )
		out.print(" "+n);
	    out.println();
	}
    }

    @Override
    public final int[] getOutDegrees() {
	int i;
	int[] degrees=new int[getVertexCount()];
	for (i=0;i<getVertexCount();i++)
	    degrees[i]=outDegree(i);
	return degrees;
    }

//     public final int[] getInDegrees() {
// 	int i;
// 	int[] degrees=new int[getVertexCount()];
// 	for (i=0;i<getVertexCount();i++) {
// 	    int[] out=getSuccessors(i);
// 	    for (int n:out)
// 		degrees[n]++;
// 	}
// 	return degrees;
//     }
    @Override
    public final int[] getInDegrees() {
	int i,n;
	final int[] indegrees=new int[this.graph.numNodes()];
	for (i=0;i<indegrees.length;i++) {
	    LazyIntIterator it=this.graph.successors(i);
	    while ( (n=it.nextInt())!=-1 )
		indegrees[n]++;
	}
	return indegrees;
    }

}

