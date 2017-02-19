
package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.exception.ASCIIGraphFormatException;
import java.io.RandomAccessFile;
import java.util.StringTokenizer;

/**
 * <p>This class implements a directed graph loaded from file. The graph is not
 * loaded into memory, only the offsets of nodes are stored.</p>
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
 * @version 0.1.1
 */
public class ASCIIOfflineGraph implements Graph {

    private final String graphName;
    private final int N;
    private final long edges;
    private final long[] offsets;
    private final RandomAccessFile reader;

    /**
     * Creates an <code>ASCIIGraph</code> from file.
     *
     * @param fileName the name of the file in which is stored the graph
     * @throws java.lang.Exception
     */
    public ASCIIOfflineGraph(final String fileName) throws Exception {
	int i;
	this.graphName=fileName;
	this.reader=new RandomAccessFile(fileName+".net","r");
	this.N=Integer.parseInt(reader.readLine());
	if (this.N<=0) {
	    this.reader.close();
	    throw new ASCIIGraphFormatException("Network dimension "+N+" less than or euqal to 0!");
	}
	this.offsets=new long[N];
        long edges = 0;
	for (i=0;i<N;i++) {
	    this.offsets[i]=this.reader.getFilePointer();
	    final String s=this.reader.readLine();
	    if (s==null) break;
	    edges+=new StringTokenizer(s).countTokens()-1;
	}
        this.edges = edges;
    }

    @Override
    public final String getName() {
	return this.graphName;
    }

    @Override
    public final int getVertexCount() {
	return this.N;
    }

    @Override
    public final long getEdgeCount() {
	return this.edges;
    }

    @Override
    public final int[] getSuccessors(final int node) {
	try {
	    int i;
	    reader.seek(offsets[node]);
	    final String s=reader.readLine();
	    if (s==null) return null;
	    final StringTokenizer st=new StringTokenizer(s);
	    final int[] output=new int[st.countTokens()-1];
	    st.nextToken();
	    for (i=0;st.hasMoreTokens();i++) {
		output[i]=Integer.parseInt(st.nextToken());
	    }
	    return output;
	} catch (Exception e) {
	    return null;
	}
    }

    /**
     * Returns an <code>int</code> array view of the predecessors of <code>node</code> in this graph.
     *
     * A vertex <code>v</code> is a predecessor of <code>node</code> if the edge <code>(v,node)</code> exists.
     *
     * @param  node the vertex whose predecessors are to be returned
     * @return      <code>null</code>
     */
    @Override
    public final int[] getPredecessors(final int node) {
	return null;
    }

    @Override
    public int[] getNeighbourhood(final int node) {
	return null;
    }

    @Override
    public final int outDegree(final int node) {
	try {
	    reader.seek(offsets[node]);
	    final String s=reader.readLine();
	    if (s==null) return 0;
	    final StringTokenizer st=new StringTokenizer(s);
	    return st.countTokens()-1;
	} catch (Exception e) {
	    return 0;
	}
    }

    @Override
    public final int inDegree(final int node) {
	return 0;
    }

    @Override
    public final boolean isNeighbor(final int a, final int b) {
	try {
	    reader.seek(offsets[a]);
	    final String s=reader.readLine();
	    if (s==null) return false;
	    final StringTokenizer st=new StringTokenizer(s);
	    st.nextToken();
	    while (st.hasMoreTokens())
		if (b==Integer.parseInt(st.nextToken())) return true;
	    return false;
	} catch (Exception e) {
	    return false;
	}
    }

    @Override
    public final int[] getOutDegrees() {
	try {
	    int i;
	    final int[] output=new int[N];
	    reader.seek(offsets[0]);
	    for (i=0;i<N;i++) {
		final String s=reader.readLine();
		final StringTokenizer st=new StringTokenizer(s);
		output[i]=st.countTokens()-1;
	    }
	    return output;
	} catch (Exception e) {
	    return null;
	}
    }

    @Override
    public final int[] getInDegrees() {
	return null;
    }

}

