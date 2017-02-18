
package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.exception.CompressedGraphFormatException;
import it.uniroma3.dia.gc.exception.MapFileFormatException;
import it.uniroma3.dia.gc.exception.MapFileMissingException;
import it.uniroma3.dia.gc.io.Reader;
import it.uniroma3.dia.gc.io.ByteReader;
import it.uniroma3.dia.gc.code.PiDecoder;

import java.io.FileReader;
import java.io.PrintStream;
import java.io.BufferedReader;
import java.util.Random;
import java.util.Arrays;

/**
 * <p>This class implements a graph compressed using the technique by Apostolico and Drovandi.
 * A detailed description of the compression format used can be found in the article cited below.</p>
 *
 * <dl>
 * <dt><b>See Also:</b></dt>
 * <dd>"Graph Compression by BFS" by Alberto Apostolico and Guido Drovandi, 2009</dd>
 * </dl>
 *
 * @author  Guido Drovandi
 * @version 0.3.1
 */
public class CompressedGraph implements Graph {

    private static final int OUT_DEGREE=0;
    private static final int FIRST_ELEM=2;
    private static final int SIGMA=3;

    private static final int ELEM=3;
    private static final int ALPHA=0; // togheter with BLOCK_H and BLOCK_W
    private static final int GAP=1;
    private static final int VALUE=2;

    private static final int THRESHOLD=2;
    private static final int THRESHOLD0=6;

    private final String graphName;
    private int N,LEVEL,FIRST,USED;
    private long E;
    private final Reader reader;
    private final PiDecoder decoder;
    private int[] line,firsts;
    private long[] offsets;
    private double version;

    public static void main(String[] args) throws Exception {
        long t1,t2;

	try {
	    t1=System.currentTimeMillis();
	    CompressedGraph cg=new CompressedGraph(args[0]);
	    t2=System.currentTimeMillis();
	    System.err.printf("Loading Time: %d ms\n",t2-t1);

// 	    cg.printASCIIGraph(System.out);
// 	    cg.printOriginalASCIIGraph(System.out);
	    cg.test(20000000,System.out);
// 	    cg.neighborTest(20000000,System.out);
	} catch (Exception e) {
	    System.err.println(e.getMessage());
	    e.printStackTrace();
	}
    }

    /**
     * Print an ASCII representation of the graph.
     *
     * @param out the stream where to write the representation
     */
    public final void printASCIIGraph(final PrintStream out) {
        int i,j;
        int[] links;
	out.println(N);
        for (i=0;i<USED;i++) {
            links=getSuccessors(i);
            Arrays.sort(links);
            out.print(i);
            for (j=0;j<links.length;j++)
                out.print(" "+links[j]);
            out.println();
        }
        for (;i<N;i++)
            out.println(i);
    }

    /**
     * Print an ASCII representation of the original graph. The <code>.map</code>
     * file is needed to perform the conversion.
     *
     * @param out the stream where to write the representation
     * @throws java.lang.Exception
     */
    public final void printOriginalASCIIGraph(final PrintStream out) throws Exception {
        int i,j;
	int map[],mapI[],links[];
	String s;
	BufferedReader br;
	map=new int[N];
	mapI=new int[N];
	try {
	    br=new BufferedReader(new FileReader(this.graphName+".map"));
	} catch (Exception e) {
	    throw new MapFileMissingException("File '"+this.graphName+".map' is missing!", e);
	}
	try {
	    for (i=0;(s=br.readLine())!=null;i++) {
		map[i]=Integer.parseInt(s);
		if (map[i]>=0) mapI[map[i]]=i;
	    }
	    br.close();
	} catch (Exception e) {
	    throw new MapFileFormatException("Wrong .map file '"+this.graphName+".map'!", e);
	}
	out.println(N);
        for (i=0;i<N;i++) {
	    if (map[i]==-1) {
		out.println(i);
	    } else {
		links=getSuccessors(map[i]);
		for (j=0;j<links.length;j++)
		    links[j]=mapI[links[j]];
		Arrays.sort(links);
		out.print(i);
		for (j=0;j<links.length;j++)
		    out.print(" "+links[j]);
		out.println();
	    }
        }
    }

    private void test(final int test, final PrintStream out) {
        int i,j,b;
        long t1,t2;
	final Random r=new Random();
        t1=System.currentTimeMillis();
        for (i=0;i<test;i++) {
            int[] o=getSuccessors(r.nextInt(USED));
	    for (j=0;j<o.length;j++)
		b=o[j];
	}
        t2=System.currentTimeMillis();
        out.printf("Successors Average Time ("+test+" tests): %3.3f us\n",(1000.*(t2-t1)/test));
    }

    private void neighborTest(final int test, final PrintStream out) {
        int i;
        long t1,t2;
	final Random r=new Random();
        t1=System.currentTimeMillis();
        for (i=0;i<test;i++)
            isNeighbor(r.nextInt(USED),r.nextInt(USED));
        t2=System.currentTimeMillis();
        out.printf("Neighbor Test Average Time ("+test+" tests): %3.3f us\n",(1000.*(t2-t1)/test));
    }

    /**
     * Creates an instance of the graph.
     *
     * @param graphName the name of the graph to be loaded
     * @throws java.lang.Exception
     */
    public CompressedGraph(final String graphName) throws Exception {
	this.graphName=graphName;
        this.reader=new ByteReader(graphName+".gc");
        this.decoder=new PiDecoder(reader);
        initialize();
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
        return this.E;
    }

    @Override
    public final int outDegree(final int node) {
        return degree(node);
    }

    @Override
    public final int inDegree(final int node) {
        return 0;
    }

    public final double getGraphVersion() {
        return this.version;
    }

    private void readVersion() throws Exception {
	if (reader.readBit())
	    version=1;
	else if (reader.readBit())
	    version=1.1;
	else
	    throw new CompressedGraphFormatException("Unrecognized format: version control ('"+version+"')!");
    }

    private void initialize() throws Exception {

	readVersion();

	try {
	    if (version==1) {
		this.N=reader.readInt();
		this.E=reader.readLong();
		this.USED=reader.readInt();
		this.LEVEL=reader.readInt();
		this.FIRST=reader.readInt();
	    } else if (version==1.1) {
		GraphInfo gi=new GraphInfo(graphName);
		this.N=gi.getVertexCount();
		this.E=gi.getEdgeCount();
		this.USED=N-gi.getIsolatedCount();
		this.LEVEL=gi.getLevel();
		this.FIRST=gi.getBFSRoot();
	    } else {
		throw new CompressedGraphFormatException("Unrecognized format: version control ('"+version+"')!");
	    }

	    if (E<0 || N<0 || USED<0 || LEVEL<0 || FIRST<0)
		throw new CompressedGraphFormatException("Corrupted Graph: some graph properties (nodes, edges, ...) are negative!");

// // 	    System.out.println("Nodes: "+getVertexCount()+" Links: "+getEdgeCount()+" Level: "+LEVEL+
// // 			       " Root: "+FIRST+" GraphVersion: "+version);

	    this.line=new int[10000];
	    this.offsets=new long[USED/LEVEL+1];
	    this.firsts=new int[USED/LEVEL+1];

	    readChunks();
	} catch (Exception e) {
	    throw new CompressedGraphFormatException("Unrecognized format!",e);
	}
    }

    final void setAlpha(final int j, final int alpha) {
	line[j]&=0x000FFFFF;
	line[j]|=(alpha-'a')<<30;
// 	line[j]&=0x0000FFFF;
// 	line[j]|=(alpha-'a')<<30;
    }

    final int getAlpha(final int j) {
	return (line[j]>>>30)+'a';
    }

    final void setBlockH(final int j, final int h) {
	line[j]&=0xC00FFFFF;
	line[j]|=h<<20;
// 	line[j]&=0xE000FFFF;
// 	line[j]|=h<<16;
    }

    final int getBlockH(final int j) {
	return (line[j]&0x3FF00000)>>>20;
// 	return (line[j]&0x1FFF0000)>>>16;
    }

    final void setBlockW(final int j, final int w) {
	line[j]&=0xFFF00000;
	line[j]|=w;
// 	line[j]&=0xFFFF0000;
// 	line[j]|=w;
    }

    final int getBlockW(final int j) {
	return line[j]&0x000FFFFF;
// 	return line[j]&0x0000FFFF;
    }

    final void readAlpha(final int alphaK, final int j) {
	switch (alphaK) {
	case 'c':
	    if (reader.readBit()) setAlpha(j,'c');
	    else if (reader.readBit()) setAlpha(j,'a');
	    else setAlpha(j,'b');
	    break;
	case 'a':
	    if (reader.readBit()) setAlpha(j,'a');
	    else if (reader.readBit()) setAlpha(j,'b');
	    else setAlpha(j,'c');
	    break;
	case 'b':
	    if (reader.readBit()) setAlpha(j,'b');
	    else if (reader.readBit()) setAlpha(j,'a');
	    else setAlpha(j,'c');
	    break;
	}
    }

    final void readBlock(final int j) {
	final int w=decoder.decode0K0()+1;
	setBlockW(j,w*ELEM);
	if (w==1)      setBlockH(j,decoder.decode0K0()+4);
	else if (w==2) setBlockH(j,decoder.decode0K0()+3);
	else if (w==3) setBlockH(j,decoder.decode0K0()+2);
	else if (w<8)  setBlockH(j,decoder.decode0K0()+1);
	else           setBlockH(j,decoder.decode0K0());
    }

    final void setValue(final int node, final int i) {
	switch (getAlpha(i)) {
	case 'd': case 'a':
	    if (i==FIRST_ELEM) {
		int r;
		if ( (r=line[FIRST_ELEM+GAP])%2==1 ) r=-(r+1);
		line[FIRST_ELEM+VALUE]=node-(r/2);
	    } else {
		line[i+VALUE]=line[i-ELEM+VALUE]+line[i+GAP]+1;
	    }
	    break;
	case 'c': line[i+VALUE]+=line[i+GAP]; break;
	case 'b': line[i+VALUE]-=line[i+GAP]+1; break;
	}
    }

    private void readChunks() throws Exception {
        int i,j,l,count,alphaK,node,first;

	first=0;
	for (node=0;node<USED;node+=LEVEL) {
	    final int parent=node/LEVEL;
	    offsets[parent]=reader.getPosition();
	    if (first<=node) first=node+1;
	    firsts[parent]=first;

	    if (reader.readBit()) {
		for (i=0;i<LEVEL;i++) {
		    int t;
		    first=(t=decoder.decode0K0())+((first<=node+i)?node+i+1:first);
		    //              System.out.print(t+" ");
		    if ( (count=decoder.decode0K0())>0 ) {
			for (j=i+1;j<=i+count;j++) {
			    if (first<=node+j) first=node+j+1;
			    first+=t;
			    //                      System.out.print(t+" ");
			}
			i=j-1;
		    }
		}
	    }
	    //      System.out.println();

	    final int piK=reader.readBits(2);
	    if (reader.readBit()) alphaK='c';
	    else if (reader.readBit()) alphaK='b';
	    else alphaK='a';

	    int lines,lastDegOut,maxDeg,degree;
	    maxDeg=0;
	    lastDegOut=-1;
	    for (i=node;i<node+LEVEL && i<USED;i++) {

		lines=decoder.decode0K0();
		//          System.out.print(lines+" ");

		if (lastDegOut==-1) {
		    line[OUT_DEGREE]=decoder.decode0K0();
		} else {
		    int r=decoder.decode0K0();
		    if (r%2==1) r=-(r+1);
		    line[OUT_DEGREE]=lastDegOut+(r/2);
		}
		if (line[OUT_DEGREE]>0) {
		    lastDegOut=line[OUT_DEGREE];
		}

		//          System.out.print(line[OUT_DEGREE]+" ");

		if ( (degree=line[OUT_DEGREE])==0) {
		    i+=lines;
		    //              System.out.println();
		    continue;
		}

		final int lim=FIRST_ELEM+ELEM*degree;
		for (j=FIRST_ELEM;j<lim;j+=ELEM) {
		    if (line.length<j+ELEM) {
			int[] tmp;
			int size=1000;
			if (line.length+size>Integer.MAX_VALUE)
			    tmp=new int[Integer.MAX_VALUE];
			else
			    tmp=new int[line.length+size];
			System.arraycopy(line,0,tmp,0,line.length);
			line=tmp;
			tmp=null;
// 			line=Arrays.copyOf(line,line.length+1000);
		    }

		    int h;
		    if (i!=node && j<maxDeg && (h=getBlockH(j))>0) {
			setBlockH(j,h-1);
			// 		    line[j]--;//setBlockH(j,getBlockH(j)-1);
			final int limW=j+getBlockW(j);
			for (l=j;l<limW;l+=ELEM) {
			    setValue(i,l);
			    //                      System.out.print((char)line[l+ALPHA]+" "+line[l+GAP]+" ");
			}
			j=l-ELEM;
			continue;
		    }
		    if (i==node || j>=maxDeg || (j!=FIRST_ELEM && line[j+VALUE]-1<=line[j-ELEM+VALUE]))
			setAlpha(j,'d');
		    else
			readAlpha(alphaK,j);

		    //              System.out.print((char)line[j+ALPHA]+" ");

		    count=0;
		    if ( (line[j+GAP]=decoder.decode0(piK))==SIGMA) {
			line[j+GAP]=decoder.decode0(piK);
			//                  System.out.print(line[j+GAP]+" ");
			setValue(i,j);

			boolean runlength,block;
			runlength=block=false;
			if (reader.readBit()) block=true;
			else if (reader.readBit()) runlength=block=true;
			else runlength=true;

			count=j;
			if (runlength) { // RUNLENGTH
			    count=(decoder.decode0K0()+((line[j+GAP]==0)?THRESHOLD0:THRESHOLD))*ELEM+j;
			    final int alpha=getAlpha(j);
			    for (l=j+ELEM;l<=count;l+=ELEM) {
				if (line.length<l+ELEM) {
				    int[] tmp;
				    int size=1000;
				    if (line.length+size>Integer.MAX_VALUE)
					tmp=new int[Integer.MAX_VALUE];
				    else
					tmp=new int[line.length+size];
				    System.arraycopy(line,0,tmp,0,line.length);
				    line=tmp;
				    tmp=null;
// 				    line=Arrays.copyOf(line,line.length+1000);
				}

				line[l+GAP]=line[j+GAP];
				line[l]=0;
				//                             setBlockH(l,0);
				// 			    setBlockW(l,0);
				setAlpha(l,alpha);
				switch (alpha) {
				case 'd': case 'a': line[l+VALUE]=line[l-ELEM+VALUE]+line[l+GAP]+1; break;
				case 'c': line[l+VALUE]+=line[l+GAP]; break;
				case 'b': line[l+VALUE]-=line[l+GAP]+1; break;
				}
				//                          System.out.print((char)line[l+ALPHA]+" "+line[l+GAP]+" ");
			    }
			}

			if (block) // BLOCK
			    readBlock(j);

			j=count;
		    } else {
			if (line[j+GAP]>SIGMA) line[j+GAP]--;
			setValue(i,j);
			//                  System.out.print(line[j+GAP]+" ");
		    }

		}

		//          System.out.println();

		if (j>maxDeg) maxDeg=j;

		while (lines-->0) {
		    i++;
		    for (j=FIRST_ELEM;j<lim;j+=ELEM)
			setValue(i,j);
		}
	    }
	}
    }

    @Override
    public final int[] getSuccessors(final int targetNode) {
        return links(targetNode);
    }

    @Override
    public final int[] getPredecessors(final int targetNode) {
        return null;
    }

    @Override
    public int[] getNeighbourhood(final int node) {
	return null;
    }

    private int[] links(final int targetNode) {
        int i,j,l,count,alphaK,first,A,B;
        int lines,lastDegOut,maxDeg;

        if (targetNode<0 || targetNode>=USED) return new int[0];

        A=B=maxDeg=0;
        lastDegOut=-1;

        final int node=targetNode-targetNode%LEVEL;
	final int parent=node/LEVEL;
	final int lastNode=node+LEVEL;
        reader.setPosition(offsets[parent]);
        first=firsts[parent];

        if (reader.readBit()) {
            int t;

            for (i=node;i<lastNode;i++) {
                first=(t=decoder.decode0K0())+((first<=i)?i+1:first);
                if (i==targetNode) A=first-(B=t);
                if ( (count=decoder.decode0K0())>0 ) {
                    for (j=i+1;j<=i+count;j++) {
                        if (first<=j) first=j+1;
                        first+=t;
                        if (j==targetNode) A=first-(B=t);
                    }
                    i=j-1;
                }
            }

//             for (i=0;i<LEVEL;i++) {
//                 first=(t=decoder.decode0K0())+((first<=node+i)?node+i+1:first);
//                 if (node+i==targetNode) A=first-(B=t);
//                 if ( (count=decoder.decode0K0())>0 ) {
//                     for (j=i+1;j<=i+count;j++) {
//                         if (first<=node+j) first=node+j+1;
//                         first+=t;
//                         if (node+j==targetNode) A=first-(B=t);
//                     }
//                     i=j-1;
//                 }
//             }

        }

        final int piK=reader.readBits(2);
        if (reader.readBit()) alphaK='c';
        else if (reader.readBit()) alphaK='b';
        else alphaK='a';

        for (i=node;i<lastNode;i++) {
//         for (i=node;i<node+LEVEL;i++) {

            lines=decoder.decode0K0();
	    line[OUT_DEGREE]=decoder.decode0K0();

            if (lastDegOut!=-1) {
                if (line[OUT_DEGREE]%2==1) line[OUT_DEGREE]=-(line[OUT_DEGREE]+1);
                line[OUT_DEGREE]=lastDegOut+(line[OUT_DEGREE]/2);
            }

            if (line[OUT_DEGREE]==0) {
                if (targetNode>=i && targetNode<=i+lines)
                    return createOutput(0,A,B);
                i+=lines;
                continue;
            }

            final int lim=FIRST_ELEM+ELEM*(lastDegOut=line[OUT_DEGREE]);
            for (j=FIRST_ELEM;j<lim;j+=ELEM) {

		int h;
                if (i!=node && j<maxDeg && (h=getBlockH(j))>0) {
                    setBlockH(j,h-1);
		    //                     line[j]--;//setBlockH(j,getBlockH(j)-1);
		    final int limW=j+getBlockW(j);
                    for (l=j;l<limW;l+=ELEM)
			setValue(i,l);
                    j=l-ELEM;
                    continue;
                }

		line[j]=0;
		//                 setBlockH(j,0);
		//                 setBlockW(j,0);

                if (i==node || j>=maxDeg || (j!=FIRST_ELEM && line[j+VALUE]-1<=line[j-ELEM+VALUE]))
                    setAlpha(j,'d');
                else
		    readAlpha(alphaK,j);

                count=0;
                if ( (line[j+GAP]=decoder.decode0(piK))==SIGMA ) {
                    line[j+GAP]=decoder.decode0(piK);
		    setValue(i,j);

                    boolean runlength,block;
                    runlength=block=false;
                    if (reader.readBit()) block=true;
                    else if (reader.readBit()) runlength=block=true;
                    else runlength=true;

                    count=j;
                    if (runlength) { // RUNLENGTH
                        count=(decoder.decode0K0()+((line[j+GAP]==0)?THRESHOLD0:THRESHOLD))*ELEM+j;
			final int alpha=getAlpha(j);
                        for (l=j+ELEM;l<=count;l+=ELEM) {
                            line[l+GAP]=line[j+GAP];
			    line[l]=0;
			    //                             setBlockH(l,0);
			    //                             setBlockW(l,0);
			    setAlpha(l,alpha);
                            switch (alpha) {
                            case 'd': case 'a': line[l+VALUE]=line[l-ELEM+VALUE]+line[l+GAP]+1; break;
                            case 'c': line[l+VALUE]+=line[l+GAP]; break;
                            case 'b': line[l+VALUE]-=line[l+GAP]+1; break;
                            }
                        }
                    }

                    if (block) // BLOCK
			readBlock(j);

                    j=count;
                } else {
                    if (line[j+GAP]>SIGMA) line[j+GAP]--;
		    setValue(i,j);
                }

            }

            if (i==targetNode)
		return createOutput(line[OUT_DEGREE],A,B);

            if (j>maxDeg) maxDeg=j;

            while (lines-->0) {
                i++;
                for (j=FIRST_ELEM;j<lim;j+=ELEM)
		    setValue(i,j);
                if (i==targetNode)
		    return createOutput(line[OUT_DEGREE],A,B);
            }
        }

        return null;
    }

    private int[] createOutput(int degree, final int A, final int B) {
        int i,j;
        final int[] output=new int[degree+B];
	for (i=0;i<B;i++) output[i]=A+i;
	degree*=ELEM;
	for (j=0;j<degree;j+=ELEM)
	    output[i++]=line[FIRST_ELEM+VALUE+j];
// 	if (degree==0) {
// 	    for (i=0;i<B;i++) output[i]=A+i;
// 	} else {
// 	    i=0;
// 	    degree*=ELEM;
// 	    for (j=0;j<degree;j+=ELEM) {
// 		if (A<line[FIRST_ELEM+VALUE+j]) {
// 		    for (int l=0;l<B;l++) output[i++]=A+l;
// 		    for (;j<degree;j+=ELEM)
// 			output[i++]=line[FIRST_ELEM+VALUE+j];
// 		    break;
// 		} else {
// 		    output[i++]=line[FIRST_ELEM+VALUE+j];
// 		}
// 	    }
// 	}
        return output;
    }

    private int degree(final int targetNode) {
        int i,j,l,count,alphaK,first,A,B;
        int lines,lastDegOut,maxDeg,degree;

        if (targetNode>=USED) return 0;

        A=B=maxDeg=0;
        lastDegOut=-1;

        final int node=targetNode-targetNode%LEVEL;
	final int parent=node/LEVEL;
        reader.setPosition(offsets[parent]);
        first=firsts[parent];

        if (reader.readBit()) {
            int t;
            for (i=0;i<LEVEL;i++) {
                first=(t=decoder.decode0K0())+((first<=node+i)?node+i+1:first);
                if (node+i==targetNode) A=first-(B=t);
                if ( (count=decoder.decode0K0())>0 ) {
                    for (j=i+1;j<=i+count;j++) {
                        if (first<=node+j) first=node+j+1;
                        first+=t;
                        if (node+j==targetNode) A=first-(B=t);
                    }
                    i=j-1;
                }
            }
        }

        final int piK=reader.readBits(2);
        if (reader.readBit()) alphaK='c';
        else if (reader.readBit()) alphaK='b';
        else alphaK='a';

        for (i=node;i<node+LEVEL;i++) {

            lines=decoder.decode0K0();

            if (lastDegOut==-1) {
                line[OUT_DEGREE]=decoder.decode0K0();
            } else {
                int r;
                if ((r=decoder.decode0K0())%2==1) r=-(r+1);
                line[OUT_DEGREE]=lastDegOut+(r/2);
            }
            if (line[OUT_DEGREE]>0) {
                lastDegOut=line[OUT_DEGREE];
            }

            if ( (degree=line[OUT_DEGREE])==0 ) {
                if (targetNode>=i && targetNode<=i+lines)
                    return B;
                i+=lines;
                continue;
            }

            final int lim=FIRST_ELEM+ELEM*degree;
            for (j=FIRST_ELEM;j<lim;j+=ELEM) {

		int h;
                if (i!=node && j<maxDeg && (h=getBlockH(j))>0) {
                    setBlockH(j,h-1);
		    //                     line[j]--;//setBlockH(j,getBlockH(j)-1);
		    final int limW=j+getBlockW(j);
                    for (l=j;l<limW;l+=ELEM)
			setValue(i,l);
                    j=l-ELEM;
                    continue;
                }

		line[j]=0;
		//                 setBlockH(j,0);
		// 		setBlockW(j,0);

                if (i==node || j>=maxDeg || (j!=FIRST_ELEM && line[j+VALUE]-1<=line[j-ELEM+VALUE]))
                    setAlpha(j,'d');
                else
		    readAlpha(alphaK,j);

                count=0;
                if ( (line[j+GAP]=decoder.decode0(piK))==SIGMA) {
                    line[j+GAP]=decoder.decode0(piK);
		    setValue(i,j);

                    boolean runlength,block;
                    runlength=block=false;
                    if (reader.readBit()) block=true;
                    else if (reader.readBit()) runlength=block=true;
                    else runlength=true;

                    count=j;
                    if (runlength) { // RUNLENGTH
                        count=decoder.decode0K0()+((line[j+GAP]==0)?THRESHOLD0:THRESHOLD);
                        count*=ELEM;
                        count+=j;
			final int alpha=getAlpha(j);
                        for (l=j+ELEM;l<=count;l+=ELEM) {
                            line[l+GAP]=line[j+GAP];
			    line[l]=0;
			    //                             setBlockH(l,0);
			    // 			    setBlockW(l,0);
			    setAlpha(l,alpha);
                            switch (alpha) {
                            case 'd': case 'a': line[l+VALUE]=line[l-ELEM+VALUE]+line[l+GAP]+1; break;
                            case 'c': line[l+VALUE]+=line[l+GAP]; break;
                            case 'b': line[l+VALUE]-=line[l+GAP]+1; break;
                            }
                        }
                    }

                    if (block) // BLOCK
			readBlock(j);

                    j=count;
                } else {
                    if (line[j+GAP]>SIGMA) line[j+GAP]--;
		    setValue(i,j);
                }

            }

            if (i==targetNode)
                return B+line[OUT_DEGREE];

            if (j>maxDeg) maxDeg=j;

            while (lines-->0) {
                i++;
                for (j=FIRST_ELEM;j<lim;j+=ELEM)
		    setValue(i,j);
                if (i==targetNode)
                    return B+line[OUT_DEGREE];
            }
        }

        return 0;
    }

    @Override
    public final boolean isNeighbor(final int n1, final int n2) {
        int i,j,l,count,alphaK,first,A,B;
        int lines,lastDegOut,maxDeg,degree;

        if (n1>=USED) return false;

        A=B=maxDeg=0;
        lastDegOut=-1;

        final int node=n1-n1%LEVEL;
	final int parent=node/LEVEL;
        reader.setPosition(offsets[parent]);
        first=firsts[parent];

        if (reader.readBit()) {
            int t;
            for (i=0;i<LEVEL;i++) {
                first=(t=decoder.decode0K0())+((first<=node+i)?node+i+1:first);
                if (node+i==n1) {
		    A=first-(B=t);
		    if (n2>=A) return (n2 < A+B);
		}
                if ( (count=decoder.decode0K0())>0 ) {
                    for (j=i+1;j<=i+count;j++) {
                        if (first<=node+j) first=node+j+1;
                        first+=t;
                        if (node+j==n1) {
			    A=first-(B=t);
			    if (n2>=A) return (n2 < A+B);
			}
                    }
                    i=j-1;
                }
            }
        } else if (n2>=first && n2>n1) return false;

        final int piK=reader.readBits(2);
        if (reader.readBit()) alphaK='c';
        else if (reader.readBit()) alphaK='b';
        else alphaK='a';

        for (i=node;i<node+LEVEL;i++) {

            lines=decoder.decode0K0();

            if (lastDegOut==-1) {
                line[OUT_DEGREE]=decoder.decode0K0();
            } else {
                int r;
                if ((r=decoder.decode0K0())%2==1) r=-(r+1);
                line[OUT_DEGREE]=lastDegOut+(r/2);
            }
            if (line[OUT_DEGREE]>0) {
                lastDegOut=line[OUT_DEGREE];
            }

            if ( (degree=line[OUT_DEGREE])==0 ) {
                if (n1>=i && n1<=i+lines)
                    return false;
                i+=lines;
                continue;
            }

            final int lim=FIRST_ELEM+ELEM*degree;
            for (j=FIRST_ELEM;j<lim;j+=ELEM) {

		int h;
                if (i!=node && j<maxDeg && (h=getBlockH(j))>0) {
                    setBlockH(j,h-1);
		    final int limW=j+getBlockW(j);
                    for (l=j;l<limW;l+=ELEM)
			setValue(i,l);
                    j=l-ELEM;
                    continue;
                }

		line[j]=0;

                if (i==node || j>=maxDeg || (j!=FIRST_ELEM && line[j+VALUE]-1<=line[j-ELEM+VALUE]))
                    setAlpha(j,'d');
                else
		    readAlpha(alphaK,j);

                count=0;
                if ( (line[j+GAP]=decoder.decode0(piK))==SIGMA) {
                    line[j+GAP]=decoder.decode0(piK);
		    setValue(i,j);

                    boolean runlength,block;
                    runlength=block=false;
                    if (reader.readBit()) block=true;
                    else if (reader.readBit()) runlength=block=true;
                    else runlength=true;

                    count=j;
                    if (runlength) { // RUNLENGTH
                        count=decoder.decode0K0()+((line[j+GAP]==0)?THRESHOLD0:THRESHOLD);
                        count*=ELEM;
                        count+=j;
			final int alpha=getAlpha(j);
                        for (l=j+ELEM;l<=count;l+=ELEM) {
                            line[l+GAP]=line[j+GAP];
			    line[l]=0;
			    setAlpha(l,alpha);
                            switch (alpha) {
                            case 'd': case 'a': line[l+VALUE]=line[l-ELEM+VALUE]+line[l+GAP]+1; break;
                            case 'c': line[l+VALUE]+=line[l+GAP]; break;
                            case 'b': line[l+VALUE]-=line[l+GAP]+1; break;
                            }
                        }
                    }

                    if (block) // BLOCK
			readBlock(j);

                    j=count;
                } else {
                    if (line[j+GAP]>SIGMA) line[j+GAP]--;
		    setValue(i,j);
                }

            }

            if (i==n1) {
		for (j=FIRST_ELEM;j<lim;j+=ELEM)
		    if (line[j+VALUE]>=n2) return line[j+VALUE]==n2;
		return false;
	    }

            if (j>maxDeg) maxDeg=j;

            while (lines-->0) {
                i++;
                for (j=FIRST_ELEM;j<lim;j+=ELEM)
		    setValue(i,j);
                if (i==n1) {
		    for (j=FIRST_ELEM;j<lim;j+=ELEM)
			if (line[j+VALUE]>=n2) return line[j+VALUE]==n2;
		    return false;
		}
            }
        }

        return false;
    }

    @Override
    public final int[] getOutDegrees() {
	return getDegrees();
    }

    @Override
    public final int[] getInDegrees() {
	return null;
    }

    private int[] getDegrees() {
	int[] degrees=new int[N];
        int i,j,l,count,alphaK,node;

	try {

	    reader.setPosition(offsets[0]);
	    for (node=0;node<USED;node+=LEVEL) {

		if (reader.readBit()) {
		    for (i=0;i<LEVEL;i++) {
			int t;
			t=decoder.decode0K0();
			if (i+node<N) degrees[i+node]+=t;
			if ( (count=decoder.decode0K0())>0 ) {
			    for (j=i+1;j<=i+count;j++) {
				if (j+node<N) degrees[j+node]+=t;
			    }
			    i=j-1;
			}
		    }
		}

		final int piK=reader.readBits(2);
		if (reader.readBit()) alphaK='c';
		else if (reader.readBit()) alphaK='b';
		else alphaK='a';

		int lines,lastDegOut,maxDeg,degree;
		maxDeg=0;
		lastDegOut=-1;
		for (i=node;i<node+LEVEL && i<USED;i++) {

		    lines=decoder.decode0K0();

		    if (lastDegOut==-1) {
			line[OUT_DEGREE]=decoder.decode0K0();
		    } else {
			int r=decoder.decode0K0();
			if (r%2==1) r=-(r+1);
			line[OUT_DEGREE]=lastDegOut+(r/2);
		    }
		    if (line[OUT_DEGREE]>0) {
			lastDegOut=line[OUT_DEGREE];
		    }

		    if ( (degree=line[OUT_DEGREE])==0) {
			i+=lines;
			continue;
		    }

		    degrees[i]+=line[OUT_DEGREE];

		    final int lim=FIRST_ELEM+ELEM*degree;
		    for (j=FIRST_ELEM;j<lim;j+=ELEM) {
// 			if (line.length<j+ELEM) {
// 			    int[] tmp;
// 			    int size=1000;
// 			    if (line.length+size>Integer.MAX_VALUE)
// 				tmp=new int[Integer.MAX_VALUE];
// 			    else
// 				tmp=new int[line.length+size];
// 			    System.arraycopy(line,0,tmp,0,line.length);
// 			    line=tmp;
// 			    tmp=null;
// // 			    line=Arrays.copyOf(line,line.length+1000);
// 			}

			int h;
			if (i!=node && j<maxDeg && (h=getBlockH(j))>0) {
			    setBlockH(j,h-1);
			    final int limW=j+getBlockW(j);
			    for (l=j;l<limW;l+=ELEM)
				setValue(i,l);
			    j=l-ELEM;
			    continue;
			}
			if (i==node || j>=maxDeg || (j!=FIRST_ELEM && line[j+VALUE]-1<=line[j-ELEM+VALUE]))
			    setAlpha(j,'d');
			else
			    readAlpha(alphaK,j);

			count=0;
			if ( (line[j+GAP]=decoder.decode0(piK))==SIGMA) {
			    line[j+GAP]=decoder.decode0(piK);
			    setValue(i,j);

			    boolean runlength,block;
			    runlength=block=false;
			    if (reader.readBit()) block=true;
			    else if (reader.readBit()) runlength=block=true;
			    else runlength=true;

			    count=j;
			    if (runlength) { // RUNLENGTH
				count=decoder.decode0K0()+((line[j+GAP]==0)?THRESHOLD0:THRESHOLD);
				count*=ELEM;
				count+=j;
				final int alpha=getAlpha(j);
				for (l=j+ELEM;l<=count;l+=ELEM) {
// 				    if (line.length<l+ELEM) {
// 					int[] tmp;
// 					int size=1000;
// 					if (line.length+size>Integer.MAX_VALUE)
// 					    tmp=new int[Integer.MAX_VALUE];
// 					else
// 					    tmp=new int[line.length+size];
// 					System.arraycopy(line,0,tmp,0,line.length);
// 					line=tmp;
// 					tmp=null;
// // 					line=Arrays.copyOf(line,line.length+1000);
// 				    }

				    line[l+GAP]=line[j+GAP];
				    line[l]=0;
				    setAlpha(l,alpha);
				    switch (alpha) {
				    case 'd': case 'a': line[l+VALUE]=line[l-ELEM+VALUE]+line[l+GAP]+1; break;
				    case 'c': line[l+VALUE]+=line[l+GAP]; break;
				    case 'b': line[l+VALUE]-=line[l+GAP]+1; break;
				    }
				}
			    }

			    if (block) // BLOCK
				readBlock(j);

			    j=count;
			} else {
			    if (line[j+GAP]>SIGMA) line[j+GAP]--;
			    setValue(i,j);
			}

		    }

		    if (j>maxDeg) maxDeg=j;

		    while (lines-->0) {
			i++;
			degrees[i]+=line[OUT_DEGREE];
			for (j=FIRST_ELEM;j<lim;j+=ELEM)
			    setValue(i,j);
		    }
		}
	    }

	} catch (Exception e) {
	    return null;
	}

	return degrees;
    }

    /************************************************************************/

    /**
     * Evaluate the page ranks of nodes of this graph.
     *
     * @param steps the number of steps to be performed
     * @param alpha the jump probability (0,1]
     * @return 
     */
    public final double[] evaluatePageRank(final int steps, final double alpha) {
        int i;
        double s1=0,s2=0;
	final double[] ranks=new double[N];
	final double[] newRanks=new double[N];
	final int[] outdegrees=getOutDegrees();
        s1=1;
        for (i=0;i<N;i++) {
            ranks[i]=1./N;
// 	    s1+=ranks[i];
            if (outdegrees[i]==0)
                s2+=ranks[i];
            newRanks[i]=0;
        }
        double K=(1+(s2-1)*alpha)/N;
        for (i=0;i<steps;i++) K=evaluatePageRankStep(K,alpha,ranks,newRanks,outdegrees);
	return ranks;
    }

    private double evaluatePageRankStep(final double K, final double rankAlpha,
					      final double[] ranks, final double[] newRanks, final int[] outdegrees) {
        int i,j,l,count,alphaK,node;
	i=0;
	try {
        reader.setPosition(offsets[0]);
        int first=0;
        for (node=0;node<USED;node+=LEVEL) {
            if (first<=node) first=node+1;

            if (reader.readBit()) {
                for (i=0;i<LEVEL;i++) {
                    int t;
                    first=(t=decoder.decode0K0())+((first<=node+i)?node+i+1:first);
                    for (j=first-t;j<first;j++)
                        newRanks[j]+=ranks[i+node]/outdegrees[i+node];
                    if ( (count=decoder.decode0K0())>0 ) {
                        for (j=i+1;j<=i+count;j++) {
                            if (first<=node+j) first=node+j+1;
                            for (l=first;l<first+t;l++)
                                newRanks[l]+=ranks[j+node]/outdegrees[j+node];
                            first+=t;
                        }
                        i=j-1;
                    }
                }
            }

            final int piK=reader.readBits(2);
            if (reader.readBit()) alphaK='c';
            else if (reader.readBit()) alphaK='b';
            else alphaK='a';

            int lines,lastDegOut,maxDeg,degree;
            maxDeg=0;
            lastDegOut=-1;
            for (i=node;i<node+LEVEL && i<USED;i++) {

                lines=decoder.decode0K0();

                if (lastDegOut==-1) {
                    line[OUT_DEGREE]=decoder.decode0K0();
                } else {
                    int r;
                    if ( (r=decoder.decode0K0())%2==1 ) r=-(r+1);
                    line[OUT_DEGREE]=lastDegOut+(r/2);
                }
                if (line[OUT_DEGREE]>0) {
                    lastDegOut=line[OUT_DEGREE];
                }

                if ( (degree=line[OUT_DEGREE])==0) {
                    i+=lines;
                    continue;
                }

                final int lim=FIRST_ELEM+ELEM*degree;
                for (j=FIRST_ELEM;j<lim;j+=ELEM) {

                    int h;
                    if (i!=node && j<maxDeg && (h=getBlockH(j))>0) {
                        setBlockH(j,h-1);
                        final int limW=j+getBlockW(j);
                        for (l=j;l<limW;l+=ELEM)
                            setValue(i,l);
                        j=l-ELEM;
                        continue;
                    }
                    if (i==node || j>=maxDeg ||
                        (j!=FIRST_ELEM && line[j+VALUE]-1<=line[j-ELEM+VALUE]))
                        setAlpha(j,'d');
                    else
                        readAlpha(alphaK,j);

                    count=0;
                    if ( (line[j+GAP]=decoder.decode0(piK))==SIGMA ) {
                        line[j+GAP]=decoder.decode0(piK);
                        setValue(i,j);

                        boolean runlength,block;
                        runlength=block=false;
                        if (reader.readBit()) block=true;
                        else if (reader.readBit()) runlength=block=true;
                        else runlength=true;

                        count=j;
                        if (runlength) { // RUNLENGTH
                            count=decoder.decode0K0()+((line[j+GAP]==0)?THRESHOLD0:THRESHOLD);
                            count*=ELEM;
                            count+=j;
                            final int alpha=getAlpha(j);
                            for (l=j+ELEM;l<=count;l+=ELEM) {
                                line[l+GAP]=line[j+GAP];
                                line[l]=0;
                                setAlpha(l,alpha);
                                switch (alpha) {
                                case 'd': case 'a': line[l+VALUE]=line[l-ELEM+VALUE]+line[l+GAP]+1; break;
                                case 'c': line[l+VALUE]+=line[l+GAP]; break;
                                case 'b': line[l+VALUE]-=line[l+GAP]+1; break;
                                }
                            }
                        }

                        if (block) // BLOCK
                            readBlock(j);

                        j=count;
                    } else {
                        if (line[j+GAP]>SIGMA) line[j+GAP]--;
                        setValue(i,j);
                    }

                }

                if (j>maxDeg) maxDeg=j;

                for (j=FIRST_ELEM;j<FIRST_ELEM+ELEM*line[OUT_DEGREE];j+=ELEM)
                    newRanks[line[j+VALUE]]+=ranks[i]/outdegrees[i];

                while (lines-->0) {
                    i++;
                    for (j=FIRST_ELEM;j<FIRST_ELEM+ELEM*line[OUT_DEGREE];j+=ELEM) {
                        setValue(i,j);
                        newRanks[line[j+VALUE]]+=ranks[i]/outdegrees[i];
                    }
                }
            }
        }
	} catch (Exception e) {
	    System.err.println(i);
	    e.printStackTrace();
	    System.exit(1);
	}

        double s1=0,s2=0;
        for (i=0;i<N;i++) {
            s1+=(ranks[i]=rankAlpha*newRanks[i]+K);
            newRanks[i]=0;
            if (outdegrees[i]==0)
                s2+=ranks[i];
        }
        return (s1*(1-rankAlpha)+s2*rankAlpha)/N;
    }

}

