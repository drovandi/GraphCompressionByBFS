
package it.uniroma3.dia.gc;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import java.io.PrintStream;

/**
 * <p>This class implements an efficient parser for BVGraph.</p>
 *
 * @author  Guido Drovandi
 * @version 0.3.1
 */
public class BVParser extends BFSParser {

    private BVGraph graph;

    /**
     * Creates an instance.
     *
     * @param graph the graph to be parsed
     * @param out if it should write the temporary file
     * @param map if it should write the map file
     * @param debug the <code>PrintStream</code> where to give information on the parsing process
     * @throws java.lang.Exception
     */
    public BVParser(final BVGraphWrapper graph, final boolean out,
		    final boolean map, final PrintStream debug) throws Exception {
	this(graph,false,false,false,out,map,false,false,debug);
    }

    /**
     * Creates an instance.
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
    public BVParser(final BVGraphWrapper graph, final boolean compression,
		    final boolean fast, final boolean stats, final boolean out,
		    final boolean map, final boolean susana, final boolean simMode,
		    final PrintStream debug) throws Exception {
	super(graph,compression,fast,stats,out,map,susana,simMode,debug);
	this.graph=graph.getBVGraph();
    }

    @Override
    protected Integer[][] nodeBFS(final int node) throws Exception {
	int i,degree;
	final Integer[][] result;
	final LazyIntIterator neighbours;
	degree=graph.outdegree(node);
	neighbours=graph.successors(node);
	result=new Integer[4][];
	result[0]=new Integer[degree];
	result[1]=new Integer[degree];
	result[2]=new Integer[1];
	result[3]=new Integer[1];
	result[2][0]=0;
	result[3][0]=0;
	for (i=0;i<degree;i++) {
	    final int n=neighbours.nextInt();
	    final int a=getBFSValue(n);
	    if (a==-1)
		result[0][result[2][0]++]=n;
	    else
		result[1][result[3][0]++]=a;
	}
	return result;
    }

}

