
package it.uniroma3.dia.gc;

import java.io.PrintStream;

/**
 *
 */
public class GraphStatistics {

    private final Graph graph;

    public static void main(String[] args) throws Exception {
	System.out.print("Loading network...");
	Graph graph=new ASCIIGraph(args[0]+".net",false);
	GraphStatistics stats=new GraphStatistics(graph);
	System.out.println(" done.");

	System.out.println("N: "+graph.getVertexCount()+" E: "+graph.getEdgeCount());

	System.out.print("OutDegree distribution...");
	stats.printOutDegreeDistr(new PrintStream(args[0]+"_outDistr.dat"));
	System.out.println(" done.");

	System.out.print("InDegree distribution...");
	stats.printInDegreeDistr(new PrintStream(args[0]+"_inDistr.dat"));
	System.out.println(" done.");

	System.out.print("|InDegree-OutDegree| distribution...");
	stats.printDiffDegreeDistr(new PrintStream(args[0]+"_diffDistr.dat"));
	System.out.println(" done.");

	long rl=stats.reciprocalLinks();
	System.out.printf("Reciprocal links: %d (%3.1f%%)\n",rl,100.*rl/graph.getEdgeCount());
    }

    public GraphStatistics(final Graph graph) {
	this.graph=graph;
    }

    public final void printOutDegreeDistr(final PrintStream out) {
	int i,maxD;
	maxD=0;
	for (i=0;i<graph.getVertexCount();i++)
	    if (maxD<graph.outDegree(i)) maxD=graph.outDegree(i);
	final int[] distr=new int[maxD+1];
	for (i=0;i<graph.getVertexCount();i++)
	    distr[graph.outDegree(i)]++;
	for (i=0;i<distr.length;i++)
	    out.println((i+1)+" "+distr[i]);
    }

    public final void printInDegreeDistr(final PrintStream out) {
	int i,maxD;
	maxD=0;
	for (i=0;i<graph.getVertexCount();i++)
	    if (maxD<graph.inDegree(i)) maxD=graph.inDegree(i);
	final int[] distr=new int[maxD+1];
	for (i=0;i<graph.getVertexCount();i++)
	    distr[graph.inDegree(i)]++;
	for (i=0;i<distr.length;i++)
	    out.println((i+1)+" "+distr[i]);
    }

    public final void printDiffDegreeDistr(final PrintStream out) {
	int i,maxD;
	maxD=0;
	for (i=0;i<graph.getVertexCount();i++) {
	    int v=Math.abs(graph.inDegree(i)-graph.outDegree(i));
	    if (maxD<v) maxD=v;
	}
	final int[] distr=new int[maxD+1];
	for (i=0;i<graph.getVertexCount();i++)
	    distr[Math.abs(graph.inDegree(i)-graph.outDegree(i))]++;
	for (i=0;i<distr.length;i++)
	    out.println((i+1)+" "+distr[i]);
    }

    public final long reciprocalLinks() {
	int i,j;
	long tot=0;
	for (i=0;i<graph.getVertexCount();i++) {
	    int[] out=graph.getSuccessors(i);
	    for (j=0;j<out.length;j++) {
		if (graph.isNeighbor(out[j],i)) tot++;
	    }
	}
	return tot;
    }

}

