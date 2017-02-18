
package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.io.Writer;
import it.uniroma3.dia.gc.code.PiEncoder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;

/**
 * <p>This class allows to compress a parsed graph (file <code>graph.parser</code>).</p>
 *
 * @author  Guido Drovandi
 * @version 0.3.1
 */
public class Compressor {

    private static final int AREA_MIN=8;
    private static final int AREA_COL_MIN=5;

    private final Writer writer;
    private final PiEncoder encoder;
    private final boolean fast;
    private long bits;

    private static long writeGraphVersion(final double version, final Writer writer) throws Exception {
	long bits=0;
	if (version==1) {
	    bits=1;
	    if (writer!=null)
		writer.writeBit(true);
	} else if (version==1.1) {
	    bits=2;
	    if (writer!=null) {
		writer.writeBit(false);
		writer.writeBit(true);
	    }
	}
	return bits;
    }

    protected Compressor(final String graph, final boolean fast, final boolean simMode) throws Exception {
        this.writer=simMode?null:new Writer(graph+".gc");
        this.encoder=new PiEncoder(writer,2000000,4);
	this.fast=fast;
	this.bits=writeGraphVersion(1.1,writer);
    }

    protected final void compressChunk(final Chunk c) throws Exception {
	c.setEncoder(encoder);
	c.compressLines();
	bits+=performCompression(c,fast,writer);
    }

    protected final long getBits() {
	return this.bits;
    }

    protected final long close() throws Exception {
	if (writer!=null)
	    writer.close();
	return bits;
    }

    /**
     * Compress the file .parser associated with the graph.
     *
     * @param graph the name of the graph to be compressed (the file graph.parser must exists)
     * @param fast  fast compression algorithm (worse compression ratio)
     * @param simMode do not write the compressed graph file
     * @param info  the stream where to write information
     * @throws java.lang.Exception
     */
    public static void evaluate(final String graph, final boolean fast, final boolean simMode, final PrintStream info) throws Exception {
        int i,j,charCount;
	long bits,counter,startMillis,secs;
	String line,log;
	String[] st;

	int LEVEL,N,USED,FIRST;
	long E;

        try (FileReader fr = new FileReader(graph+".info")) {
            try (BufferedReader finfo=new BufferedReader(fr)) {
                E=N=USED=LEVEL=FIRST=-1;

                while ( (line=finfo.readLine())!=null ) {
                    st=line.split("=");
                    if (st.length!=2) continue;

                    if (st[0].trim().equals("Nodes"))
                        N=Integer.parseInt(st[1].trim());
                    else if (st[0].trim().equals("Edges"))
                        E=Long.parseLong(st[1].trim());
                    else if (st[0].trim().equals("Isolated"))
                        USED=Integer.parseInt(st[1].trim());
                    else if (st[0].trim().equals("Level"))
                        LEVEL=Integer.parseInt(st[1].trim());
                    else if (st[0].trim().equals("BfsRoot"))
                        FIRST=Integer.parseInt(st[1].trim());
                }
            }
        }

	if (E<0 || N<0 || USED<0 || LEVEL<0 || FIRST<0) {
	    System.err.println("ERROR: The info file '"+graph+".info' is corrupted!");
	    System.exit(1);
	}
	USED=N-USED;

	info.println("Nodes: "+N+" Links: "+E+" Level: "+LEVEL+" Root: "+FIRST);
	info.println();

 	final Writer writer;
        final PiEncoder encoder;
        final BufferedReader fin;

        writer=simMode?null:new Writer(graph+".gc");
        encoder=new PiEncoder(writer,2000000,4);
        fin=new BufferedReader(new FileReader(graph+".parser"));

        bits=0;

	if (writer!=null) {
	    bits+=writeGraphVersion(1,writer);
	    bits+=writer.writeBits(N,32);
	    bits+=writer.writeLong(E);
	    bits+=writer.writeBits(USED,32);
	    bits+=writer.writeBits(LEVEL,32);
	    bits+=writer.writeBits(FIRST,32);
	}

	int[][] chunk=new int[LEVEL][];
	int[] traverseList=new int[LEVEL];

	boolean run=true;
	double lastP=0;
	counter=0;
	Chunk c=new Chunk(LEVEL,encoder);

	startMillis=System.currentTimeMillis();

	log="Completed:   0% - ? bits/link (? bits/link) est ? m ? s";
	charCount=log.length();
	info.print(log);

	while (run) {

	    int m,lines=0;

	    // INIZIO : Leggo un chunk

	    m=c.parse(fin);
	    if (m<LEVEL) run=false;
	    counter+=m;

	    // FINE : Leggo un chunk

	    bits+=performCompression(c,fast,writer);

	    double v=100.*counter/N;
	    if (lastP+2<=v) {
		secs=(long)((100.-v)*(System.currentTimeMillis()-startMillis)/(1000*v));
		info.flush();
		for (i=0;i<charCount;i++) info.printf("\b");
		log=String.format("Completed: %3d%% - %3.3f bits/link (%3.3f bits/link) est %d m %d s",
				  (int)v,1.*bits/E,(1.*bits/counter)*(1.*N/E),secs/60,secs%60);
		info.print(log);
		for (i=log.length();i<charCount;i++) info.printf(" ");
		charCount=log.length()>charCount?log.length():charCount;
		lastP=v;
	    }

	}
	secs=(long)(System.currentTimeMillis()-startMillis)/1000;
	info.flush();
	for (i=0;i<charCount;i++) info.printf("\b");
	log=String.format("Completed: 100%% - %3.3f bits/link (Size: %d bits) in %d m %d s",1.*bits/E,bits,secs/60,secs%60);
	info.print(log);
	for (i=log.length();i<charCount;i++) info.printf(" ");
	info.println();

	fin.close();

	if (writer!=null)
	    writer.close();

	info.println();

// 	printStatistics(System.out);
    }

    private static long performCompression(final Chunk c, final boolean fast, final Writer writer) throws Exception {
	int area;
	for (area=300+AREA_MIN;area>=AREA_MIN;area-=5)
	    c.blockSearch(area,fast);
	for (area=c.getLevel();area>=AREA_COL_MIN;area-=5)
	    c.columnSearch(area);

	long bits=c.setCompressionParams();
	if (writer!=null) {
	    long tmp=c.writeChunk(writer);
	    if ( bits!=tmp )
		System.err.println("BITS DIFF "+(bits-tmp));
	}
	return bits;
    }

    public final void printStatistics(final PrintStream out) {
	int i;
	out.println();
	out.println("Statistics");
	out.println();
	out.println("\tCodes:");
	for (i=0;i<Chunk.stat.length;i++)
	    out.println("\tPi"+i+": "+Chunk.stat[i]);
	out.println();

	out.println("\tTypes:");
	out.println("\ta: "+Chunk.statistics['a'-'a']);
	out.println("\tb: "+Chunk.statistics['b'-'a']);
	out.println("\tc: "+Chunk.statistics['c'-'a']);
	out.println("\tx: "+Chunk.statistics['x'-'a']);
	out.println();

	out.println("\tSigma flags:");
	out.println("\t1: "+Chunk.statistics['m'-'a']);
	out.println("\t2: "+Chunk.statistics['n'-'a']);
	out.println("\t3: "+Chunk.statistics['o'-'a']);
    }

}

