
package it.uniroma3.dia.gc.algorithms;

import it.uniroma3.dia.gc.Graph;
import it.uniroma3.dia.gc.CompressedGraph;

public final class PageRank {

    private int N;
    private double K;
    private final double alpha;
    private double[] ranks,newRanks;
    private int[] outdegrees;
    private final Graph graph;
    private final CompressedGraph compressedPageRank;

    public static void main(String[] args) throws Exception {
	int i,j;
        long t1,t2;
	double[] ranks;

	if (args.length<3) {
	    System.err.println("Usage:");
	    System.err.println("\tjava it.uniroma3.dia.gc.algorithms.PageRank GRAPH IT P");
	    System.err.println();
	    System.err.println("\tGRAPH - the GRAPH.gc to be loaded");
	    System.err.println("\tIT    - the number of iterations");
	    System.err.println("\tP     - the probability of jump");
	    System.exit(1);
	}

	System.out.print("Loading Graph...");
        t1=System.currentTimeMillis();
        Graph           cg1=null;
        CompressedGraph cg2=null;
	if (args.length==4 && args[3].equals("1"))
	    cg1=new it.uniroma3.dia.gc.ASCIIGraph(args[0]);
	else
	    cg2=new CompressedGraph(args[0]);
        t2=System.currentTimeMillis();
	System.out.println(" done.");
        System.out.printf("Time: %d ms\n",t2-t1);

	System.out.print("Creating PageRank...");
        t1=System.currentTimeMillis();
        PageRank pr=null;
	if (args.length==4 && args[3].equals("1"))
	    pr=new PageRank(cg1,Double.parseDouble(args[2]));
	else
	    pr=new PageRank(cg2,Double.parseDouble(args[2]));
        t2=System.currentTimeMillis();
	System.out.println(" done.");
        System.out.printf("Time: %d ms\n",t2-t1);

	final int steps=Integer.parseInt(args[1]);
	System.out.print("Computing ranks...");
        t1=System.currentTimeMillis();
	pr.evaluate(steps);
        t2=System.currentTimeMillis();
	System.out.println(" done.");
        System.out.printf("Time: %d ms - Time/Iteration: %5.2f ms\n",t2-t1,1.*(t2-t1)/steps);

	ranks=pr.getRanks();

	for (j=1;j<=20;j++) {
	    int maxI=-1;
	    double max=-1;
	    for (i=0;i<ranks.length;i++) {
		if (max<ranks[i]) {
		    max=ranks[i];
		    maxI=i;
		}
	    }
	    ranks[maxI]=0;
	    System.out.printf("%2d. Page: %10d\tRank: %1.5f\n",j,maxI,max);
	}
    }

    public PageRank(final Graph graph, final double alpha) throws Exception {
	int i;
	this.graph=graph;
	this.alpha=alpha;
	this.compressedPageRank=null;
	this.N=graph.getVertexCount();
	this.ranks=new double[N];
	this.newRanks=new double[N];
	this.outdegrees=graph.getOutDegrees();
	double s1=0,s2=0;
	s1=1;
	for (i=0;i<N;i++) {
	    this.ranks[i]=1./N;
// 	    s1+=this.ranks[i];
	    if (outdegrees[i]==0)
		s2+=this.ranks[i];
	}
	this.K=(1+(s2-1)*alpha)/N;
// 	this.K=(s1*(1-alpha)+s2*alpha)/N;
    }

    public PageRank(final CompressedGraph graph, final double alpha) throws Exception {
	this.graph=null;
	this.alpha=alpha;
	this.compressedPageRank=graph;
    }

    public void evaluate(final int steps) {
	int i,j;
	double s1,s2;
	if (graph!=null) {
	    for (j=0;j<steps;j++) {
		for (i=0;i<N;i++) {
		    int[] out=graph.getSuccessors(i);
		    for (int n:out)
			newRanks[n]+=ranks[i]/outdegrees[i];
		}
		s1=s2=0;
		for (i=0;i<N;i++) {
		    s1+=(ranks[i]=alpha*newRanks[i]+K);
		    newRanks[i]=0;
		    if (outdegrees[i]==0)
			s2+=ranks[i];
		}
		K=(s1*(1-alpha)+s2*alpha)/N;
	    }
	} else if (compressedPageRank!=null) {
// 	    compressedPageRank.evaluate(steps);
// 	    ranks=compressedPageRank.getRanks();
	    ranks=compressedPageRank.evaluatePageRank(steps,alpha);
	}
    }

    public double[] getRanks() {
	return this.ranks;
    }

    void reset() {
	if (graph!=null) {
	    double s1=0,s2=0;
	    s1=1;
	    for (int i=0;i<N;i++) {
		ranks[i]=1./N;
		if (outdegrees[i]==0)
		    s2+=ranks[i];
		newRanks[i]=0;
	    }
	    K=(1+(s2-1)*alpha)/N;
	} else {
// 	    compressedPageRank.reset();
	}
    }

//     public final static double[] evaluate(final Graph graph, final int steps,
// 					  final double alpha) {
// 	int i,j,N;
// 	double K,s1=0,s2=0;
// 	double[] ranks,newRanks;
// 	final int[] outdegrees=graph.getOutDegrees();
// 	N=graph.getVertexCount();
// 	ranks=new double[N];
// 	newRanks=new double[N];
// 	s1=1;
// 	for (i=0;i<N;i++) {
// 	    ranks[i]=1./N;
// // 	    s1+=ranks[i];
// 	    if (outdegrees[i]==0)
// 		s2+=ranks[i];
// 	}
// 	K=(1+(s2-1)*alpha)/N;
// // 	K=(s1*(1-alpha)+s2*alpha)/N;
// 	for (j=0;j<steps;j++) {
// 	    for (i=0;i<N;i++) {
// 		int[] out=graph.getSuccessors(i);
// 		for (int n:out)
// 		    newRanks[n]+=ranks[i]/outdegrees[i];
// 	    }
// 	    s1=s2=0;
// 	    for (i=0;i<N;i++) {
// 		s1+=(ranks[i]=alpha*newRanks[i]+K);
// 		newRanks[i]=0;
// 		if (outdegrees[i]==0)
// 		    s2+=ranks[i];
// 	    }
// 	    K=(s1*(1-alpha)+s2*alpha)/N;
// 	}
// 	return ranks;
//     }

}

