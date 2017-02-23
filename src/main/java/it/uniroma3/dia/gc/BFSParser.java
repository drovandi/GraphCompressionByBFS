
package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.comparator.BFSComparator;
import java.io.PrintStream;
import java.util.ArrayDeque;
import java.util.Arrays;

/**
 * <p>This is a parser for a <code>Graph</code>.</p>
 *
 * <p>This class produces the chunks that will be compressed. Each chunk
 * is produced by performing two pahses: a BFS, that removes the edges belonging
 * to the BFS-tree, and a comparison of the remaning adjacency list of a node with the
 * one of the previous node.</p>
 *
 * <p>A parser also writes a mapping file (named with the graph name followed by <code>.map</code>) that
 * shows the new indices of nodes: In the <i>i</i>th line there is the new index for node <i>i</i>.
 * A new index equals to <code>-1</code> means that the node has outdegree and indegree equals to
 * <code>0</code> then it is not represented.</p>
 *
 * @author  Guido Drovandi
 * @version 0.3.5
 */
public class BFSParser {

    private int level,visitedNodes,firstNode;
    private int first,removed,counter;
    private int lastP,nodeUsed;
    private int[] traverse;
    private long bfsMillis;
    private boolean zeros;
    private Chunk chunk;
    private String log;

    private final int N;
    private final long E;
    private final int[] bfs;
    private final Graph graph;
    private final long[] statistics;
    private final Compressor compressor;
    private final ArrayDeque<Integer> queue;
    private final PrintStream out,map,debug;
    private final boolean stats;
    private final BFSComparator comparator;

    /**
     * Creates a parser of <code>graph</code>
     *
     * @param graph the graph to be parsed
     * @param out write the temporary file
     * @param map write the map file
     * @param debug the <code>PrintStream</code> where to give information on the parsing process
     * @throws java.lang.Exception
     */
    public BFSParser(final Graph graph,
		     final boolean out,
		     final boolean map,
                     final BFSComparator comparator,
		     final PrintStream debug) throws Exception {
	this(graph,false,false,false,out,map,false,false,comparator,debug);
    }

    /**
     * Creates a parser of <code>graph</code>
     *
     * @param graph the graph to be parsed
     * @param compression direct compression of the graph. If <code>false</code> only the temporary file is created
     * @param fast faster compression of the graph
     * @param stats
     * @param out write the temporary file
     * @param map write the map file
     * @param susana do not relabel the nodes (no BFS effects)
     * @param simMode do not write the compressed graph file
     * @param debug the <code>PrintStream</code> where to give information on the parsing process
     * @throws java.lang.Exception
     */
    public BFSParser(final Graph graph,
		     final boolean compression,
		     final boolean fast,
		     final boolean stats,
		     final boolean out,
		     final boolean map,
		     final boolean susana,
		     final boolean simMode,
                     final BFSComparator comparator,
		     final PrintStream debug) throws Exception {
	int i;
	this.graph=graph;
	this.N=graph.getVertexCount();
	this.E=graph.getEdgeCount();
	this.statistics=new long['z'-'a'+1];
	this.queue=new ArrayDeque<>(100000);
	if (susana) {
	    this.bfs=null;
	} else {
	    this.bfs=new int[N];
	    for (i=0;i<N;i++)
		this.bfs[i]=-1;
	}
	this.traverse=null;
	this.stats=stats;
	if (compression) this.compressor=new Compressor(graph.getName(),fast,simMode);
	else             this.compressor=null;
	if (out) this.out=new PrintStream(graph.getName()+".parser");
	else     this.out=null;
	if (map) this.map=new PrintStream(graph.getName()+".map");
	else     this.map=null;
        comparator.setGraph(graph);
        comparator.setParser(this);
        this.comparator = comparator;
	this.debug=debug;
    }

    private void addToQueue(final int node) {
	if (bfs==null) // susana
	    return;

	bfs[node]=nodeUsed++;
	queue.add(node);
// 	System.err.println(node);

        comparator.addToQueue(node);
    }

    /**
     * Returns the new <code>id</code> of <code>node</code>.
     *
     * @param node the queried <code>node</code>
     * @return     the new <code>id</code> if assigned
     */
    public final int getBFSValue(final int node) {
	if (bfs==null)
	    return node;
	return bfs[node];
    }

    protected Integer[][] nodeBFS(final int node) throws Exception {
	final int[] neighbours;
	final Integer[][] result;

	neighbours=graph.getSuccessors(node);

	result=new Integer[4][];
	result[0]=new Integer[neighbours.length];
	result[1]=new Integer[neighbours.length];
	result[2]=new Integer[1];
	result[3]=new Integer[1];
	result[2][0]=0;
	result[3][0]=0;

	for (int n : neighbours) {
	    final int a=getBFSValue(n);
	    if (a==-1)
		result[0][result[2][0]++]=n;
	    else
		result[1][result[3][0]++]=a;
	}
        
        if (bfs!=null) {
            comparator.nodeBFS(node);
        }

	return result;
    }

    private void BFS(final int node) throws Exception {
	int i;

	Integer[][] r=nodeBFS(node);
	traverse[visitedNodes]=r[2][0];

	if (traverse[visitedNodes]>0) {
	    zeros=false;
            comparator.beforeSort(r[0], traverse[visitedNodes]);
            Arrays.sort(r[0],0,traverse[visitedNodes],comparator);
            comparator.afterSort(r[0], traverse[visitedNodes]);
	    for (i=0;i<traverse[visitedNodes];i++)
		addToQueue(r[0][i]);
	    removed+=traverse[visitedNodes];
	}

	chunk.setOutDegree(visitedNodes,graph.outDegree(node)-traverse[visitedNodes],counter);

	Arrays.sort(r[1],0,r[3][0]);
	chunk.setValues(visitedNodes,r[1],r[3][0]);

	if (first<=getBFSValue(node))
	    first=getBFSValue(node)+1;
	first+=traverse[visitedNodes];

	counter++;
	visitedNodes++;

	if (visitedNodes==level) {
	    chunk.setTraverseList(traverse,firstNode,zeros,visitedNodes);
	    if (out!=null) chunk.printChunk(out);
	    if (compressor!=null) compressor.compressChunk(chunk);
	    chunk=new Chunk(level);
	    firstNode=first;
	    visitedNodes=0;
	    zeros=true;
	    debugStatus();
	}
    }

    /**
     * Parse the graph using a BFS starting from the specified node.
     *
     * @param level  the compression level
     * @param root   the root of the BFS-tree
     * @throws java.lang.Exception
     */
    public final void evaluate(final int level, final int root) throws Exception {
	int i,first;

	this.level=level;
	this.lastP=0;
	this.counter=0;
	this.first=1;
	this.firstNode=1;
	this.visitedNodes=0;
	this.traverse=new int[level];
	this.zeros=true;
	this.chunk=new Chunk(level);
	this.removed=0;
	this.nodeUsed=0;
	this.bfsMillis=System.currentTimeMillis();

	first=root;

	if (compressor!=null) {
	    log="  0% - ? bits/link (? bits/link) in ? m ? s est ? m";
	    debug.print("BFS Compression: "+log);
	} else {
	    log="  0% in ? m ? s est ? m";
	    debug.print("BFS Completed: "+log);
	}

	if (bfs==null) {
	    first=0;
	    nodeUsed=N;
	} else {
	    for (i=0;graph.outDegree(first)==0 && i<N; i++)
		first=(first+1)%N;
	    addToQueue(first);
	}

	//final Random random=new Random(root);
	while (true) {
	    if (bfs==null)
		BFS(first);
	    else
		BFS(queue.remove());
	    if (queue.isEmpty()) {

		// int max=-1;
		// int node=-1;
		// for (i=0;i<N;i++) {
		//     if (getBFSValue(i)==-1 && graph.outDegree(i)>0) {
		// 	final int[] outs=graph.getSuccessors(i);
		// 	int count=0;
		// 	for (j=0;j<outs.length;j++)
		// 	    if (getBFSValue(outs[j])!=-1) count++;
		// 	if (count>max) {
		// 	    max=count;
		// 	    node=i;
		// 	}
		//     }
		// }
		// if (node==-1) break;
		// first=node;
		// addToQueue(first);

		// int node=random.nextInt(N);
		if (bfs==null) {
		    first++;
		    if (first==N) break;
		} else {
		    int node=first;
		    for (i=0;i<N;i++) {
			if (getBFSValue(node)==-1 && graph.outDegree(node)>0) break;
			node=(node+1)%N;
		    }
		    if (i==N) break;
		    first=node;
		    addToQueue(first);
		}
	    }
	}
	if (visitedNodes>0) {
	    for (i=visitedNodes;i<traverse.length;i++)
		traverse[i]=0;
	    chunk.setTraverseList(traverse,firstNode,zeros,visitedNodes);
	    chunk.printChunk(out);
	    if (compressor!=null) compressor.compressChunk(chunk);
	}
	long bits=0;
	if (compressor!=null) bits=compressor.close();
	if (out!=null) out.close();

	for (i=0;i<log.length();i++)
	    debug.printf("\b");
	long secs=(long)((System.currentTimeMillis()-bfsMillis)/1000);
	String log2=String.format("100%% (Links removed: %d) in %d m %d s",removed,secs/60,secs%60);
	if (log2.length()<log.length()) {
	    for (i=log2.length();i<log.length();i++)
		log2+=" ";
	}
	debug.println(log2);

	debug.print("Writing .map file...");
	if (map!=null) {
	    if (bfs==null) {
		for (i=0;i<N;i++)
		    map.println(i);
	    } else {
		for (i=0;i<bfs.length;i++)
		    map.println(getBFSValue(i));
	    }
	    map.close();
	    debug.println(" done.");
	} else {
	    debug.println(" skipped.");
	}

	GraphInfo gi=new GraphInfo(graph.getName(),N,E,N-nodeUsed,level,root);
	gi.writeGraphInfo();

	if (bits>0) {
	    debug.printf("\nSize: %d bits (%.3f bits/link)\n",bits,1.*bits/E);
	}

	if (stats)
	    compressor.printStatistics(debug);

// 	debug.println("Statistics");
// 	for (i=0;i<statistics.length;i++) {
// 	    if (statistics[i]!=0)
// 		debug.println("\t"+(char)('a'+i)+": "+statistics[i]);
// 	}
    }

    private void printChunk(Chunk c) {
	c.printChunk(out);
    }

    private void debugStatus() {
	int i;
	int v=(int)Math.floor(100.*counter/N);
	if (lastP+2<=v) {
	    long time=(System.currentTimeMillis()-bfsMillis)/1000;
	    long secs=(long)((100.-v)*(System.currentTimeMillis()-bfsMillis)/(1000*v));
	    for (i=0;i<log.length();i++)
		debug.printf("\b");
	    String log2=String.format("%3d%%",v);
	    if (compressor!=null) {
		long bits=compressor.getBits();
		log2+=String.format(" - %3.3f bits/link (%3.3f bits/link)",1.*bits/E,(1.*bits/counter)*(1.*N/E));
	    }
	    log2+=String.format(" in %d m %2d s est %d m",time/60,time%60,(int)Math.ceil(secs/60.));
	    if (log2.length()<log.length()) {
		for (i=log2.length();i<log.length();i++)
		    log2+=" ";
	    }
	    log=log2;
	    debug.print(log);
	    lastP=v;
	}
    }

}

