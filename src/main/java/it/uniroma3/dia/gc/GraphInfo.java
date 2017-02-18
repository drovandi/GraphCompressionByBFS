
package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.exception.CompressedGraphFormatException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;

public class GraphInfo {

    private final String graphName;
    private int N,ISOLATED,LEVEL,ROOT;
    private long E;

    public GraphInfo(final String graphName, final int N, final long E,
		     final int ISOLATED, final int LEVEL, final int ROOT) {
	this.graphName=graphName;
	this.N=N;
	this.E=E;
	this.ISOLATED=ISOLATED;
	this.LEVEL=LEVEL;
	this.ROOT=ROOT;
    }

    public GraphInfo(final String graphName) throws Exception {
	String line;
	this.graphName=graphName;
	try (FileReader fr = new FileReader(graphName+".info")) {
            try (BufferedReader finfo=new BufferedReader(fr)) {
                while ( (line=finfo.readLine())!=null ) {
                    final String[] st=line.split("=");
                    if (st.length!=2) continue;
                    if (st[0].trim().equals("Nodes"))
                        N=Integer.parseInt(st[1].trim());
                    else if (st[0].trim().equals("Edges"))
                        E=Long.parseLong(st[1].trim());
                    else if (st[0].trim().equals("Isolated"))
                        ISOLATED=Integer.parseInt(st[1].trim());
                    else if (st[0].trim().equals("Level"))
                        LEVEL=Integer.parseInt(st[1].trim());
                    else if (st[0].trim().equals("BfsRoot"))
                        ROOT=Integer.parseInt(st[1].trim());
                }
            }
        }
	if (E<0 || N<0 || ISOLATED<0 || LEVEL<0 || ROOT<0)
	    throw new CompressedGraphFormatException("Corrupted Graph: some graph properties (nodes, edges, ...) are negative!");
    }

    public final String getGraphName() {
	return this.graphName;
    }

    public final int getVertexCount() {
	return this.N;
    }

    public final long getEdgeCount() {
	return this.E;
    }

    public final int getLevel() {
	return this.LEVEL;
    }

    public final int getIsolatedCount() {
	return this.ISOLATED;
    }

    public final int getBFSRoot() {
	return this.ROOT;
    }

    public final void writeGraphInfo() throws Exception {
	try (PrintStream info=new PrintStream(graphName+".info")) {
            info.println("Nodes = "+N);
            info.println("Edges = "+E);
            info.println("AvgDegree = "+((int)(100.*E/N))/100.);
            info.println("Isolated = "+ISOLATED);
            info.println("Level = "+LEVEL);
            info.println("BfsRoot = "+ROOT);
        }
    }

}

