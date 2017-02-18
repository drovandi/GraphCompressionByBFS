
package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.exception.ASCIIGraphFormatException;
import java.util.Random;

/**
 * <p>This application allows to compress graphs.</p>
 *
 * <p>Usage: <code>java it.uniroma3.dia.gc.Main COMMANDS GRAPH [OPTIONS]</code></p>
 *
 * <p><code>COMMANDS</code>:
 * <ul>
 * <li><code>a</code> - parse an ASCII graph file (named <code>GRAPH.net</code>)</li>
 * <li><code>o</code> - parse an ASCII graph file offline (named <code>GRAPH.net</code>)</li>
 * <li><code>g</code> - parse a compressed graph (named <code>GRAPH.gc</code>)</li>
 * <li><code>b</code> - parse a BV graph file (named <code>GRAPH.graph</code>)</li>
 * <li><code>c</code> - compression of a parsed graph file (named <code>GRAPH.parser</code>)</li>
 * <li><code>x</code> - direct compression of a graph (does not create the temporary file)</li>
 * <li><code>p</code> - create the temporary file GRAPH.parser</li>
 * </ul>
 * </p>
 *
 * <p><code>OPTIONS</code>:
 * <ul>
 * <li><code>-l LEVEL</code> - set the compression level (default 1000)</li>
 * <li><code>-r NODE</code> - set the root of the BFS (default random, not with -s)</li>
 * <li><code>-map</code> - writes the map file</li>
 * <li><code>-f</code> - faster compression</li>
 * <li><code>-s</code> - use original ids (BFS does not relabel nodes)</li>
 * <li><code>-sim</code> - simulation mode, do not write the compressed graph file (only with 'x' or 'c')</li>
 * </ul>
 * </p>
 *
 * <p>Usage examples:
 * <ul>
 * <li><code>java it.uniroma3.dia.gc.Main a  cnr-2000 -l 1000 -map</code></li>
 * <li><code>java it.uniroma3.dia.gc.Main c  cnr-2000</code></li>
 * <li><code>java it.uniroma3.dia.gc.Main ac cnr-2000 -l 8</code></li>
 * <li><code>java it.uniroma3.dia.gc.Main ax cnr-2000 -l 8</code></li>
 * <li><code>java it.uniroma3.dia.gc.Main ax cnr-2000 -l 8 -f</code></li>
 * </ul>
 * </p>
 *
 * @author  Guido Drovandi
 * @version 0.3.3
 */
public final class Main {

    private Main() {}

    private static void printHelp() {
	System.err.println("Graph Compression - version 0.3.3");
	System.err.println();
	System.err.println("Usage:");
	System.err.println("\t java it.uniroma3.dia.gc.Main COMMANDS GRAPH [OPTIONS]");
	System.err.println();
	System.err.println("COMMANDS:");
	System.err.println("\t a - parse an ASCII graph file (GRAPH.net)");
	System.err.println("\t o - parse an ASCII graph file offline (GRAPH.net)");
	System.err.println("\t g - parse a compressed graph (GRAPH.gc)");
	System.err.println("\t b - parse a BV graph file (GRAPH.graph)");
	System.err.println("\t c - compression of a parsed graph file (GRAPH.parser)");
	System.err.println("\t x - direct compression of a graph (does not create the temporary file)");
	System.err.println("\t p - create the temporary file GRAPH.parser");
	System.err.println();
	System.err.println("OPTIONS:");
	System.err.println("\t-l LEVEL    - set the compression level (default 1000)");
	System.err.println("\t-r NODE     - set the root of the BFS (default random, not with -s)");
	System.err.println("\t-map        - writes the map file");
	System.err.println("\t-f          - faster compression");
	System.err.println("\t-s          - use original ids (BFS does not relabel nodes)");
	System.err.println("\t-sim        - simulation mode, do not write the compressed graph file (only with 'x' or 'c')");
	System.err.println();
	System.err.println("EXAMPLES:");
	System.err.println("\t java it.uniroma3.dia.gc.Main a  cnr-2000 -l 1000 -map");
	System.err.println("\t java it.uniroma3.dia.gc.Main c  cnr-2000");
	System.err.println("\t java it.uniroma3.dia.gc.Main ac cnr-2000 -l 8");
	System.err.println("\t java it.uniroma3.dia.gc.Main ax cnr-2000 -l 8");
	System.err.println("\t java it.uniroma3.dia.gc.Main ax cnr-2000 -l 8 -f");
	System.err.println();
    }

    public static void main(String[] args) {
	if (args.length<2) {
	    printHelp();
	    System.exit(0);
	}
	int i,level,root;
	boolean bv,ascii,asciiOffline,compressedGraph,compression,mapFile,stats,fast,directCompression,printTemp,susana,simMode;
	bv=ascii=asciiOffline=compressedGraph=compression=mapFile=stats=fast=directCompression=printTemp=susana=simMode=false;
	char[] commands=args[0].toCharArray();
	String network=args[1];
	if (commands.length==0) {
	    System.err.println("COMMANDS ERROR!");
	    printHelp();
	    System.exit(1);
	}
	for (i=0;i<commands.length;i++) {
	    switch (commands[i]) {
	    case 'a': ascii=true; break;
	    case 'o': asciiOffline=true; break;
	    case 'g': compressedGraph=true; break;
	    case 'b': bv=true; break;
	    case 'c': compression=true; break;
	    case 'x': directCompression=true; break;
	    case 'p': printTemp=true; break;
	    }
	}
	if (!ascii && !bv && !asciiOffline && !compressedGraph && !compression) {
	    System.err.println("COMMANDS ERROR!");
	    System.err.println("PLEASE CHOOSE AT LEAST ONE COMMAND aobgc!");
	    printHelp();
	    System.exit(1);
	}
	if ((ascii && bv) || (asciiOffline && bv) || (ascii && asciiOffline) || (compressedGraph && ascii)
	    || (compressedGraph && asciiOffline) || (compressedGraph && bv)) {
	    System.err.println("COMMANDS ERROR!");
	    System.err.println("ONLY ONE COMMAND BETWEEN aobg AMMITTED!");
	    printHelp();
	    System.exit(1);
	}
	if (compression && directCompression) {
	    System.err.println("COMMANDS ERROR!");
	    System.err.println("ONLY ONE COMMAND BETWEEN cx AMMITTED!");
	    printHelp();
	    System.exit(1);
	}
	if (directCompression && !(ascii || bv || asciiOffline || compressedGraph)) {
	    System.err.println("COMMANDS ERROR!");
	    System.err.println("x COMMAND ONLY ALLOWED IF ONE BETWEEN aobg IS SPECIFIED!");
	    printHelp();
	    System.exit(1);
	}
	if (printTemp && !(ascii || bv || asciiOffline || compressedGraph)) {
	    System.err.println("COMMANDS ERROR!");
	    System.err.println("p COMMAND ONLY ALLOWED IF ONE BETWEEN aobg IS SPECIFIED!");
	    printHelp();
	    System.exit(1);
	}
	level=1000;
	root=-1;
	if ((ascii || bv || asciiOffline || compressedGraph) && args.length>2) {
	    for (i=2;i<args.length;i++) {
		if (args[i].equals("-l")) {
		    if (i+1>=args.length) {
			System.err.println("OPTIONS ERROR!");
			printHelp();
			System.exit(1);
		    }
		    try {
			level=Integer.parseInt(args[i+1]);
			if (level<=0) throw new Exception();
		    } catch (Exception e) {
			System.err.println("WRONG LEVEL '"+args[i+1]+"'. IT MUST BE AN INTEGER GREATER THAN 0!");
			printHelp();
			System.exit(1);
		    }
		    i++;
		} else if (args[i].equals("-map")) {
		    mapFile=true;
		} else if (args[i].equals("-stats")) {
		    stats=true;
		} else if (args[i].equals("-r")) {
		    if (i+1>=args.length) {
			System.err.println("OPTIONS ERROR!");
			printHelp();
			System.exit(1);
		    }
		    try {
			root=Integer.parseInt(args[i+1]);
			if (root<0) throw new Exception();
		    } catch (Exception e) {
			System.err.println("WRONG ROOT NODE '"+args[i+1]+"'. IT MUST BE AN INTEGER GREATER THAN OR EQUAL TO 0!");
			printHelp();
			System.exit(1);
		    }
		    i++;
		} else if (args[i].equals("-f")) {
		    fast=true;
		} else if (args[i].equals("-s")) {
		    susana=true;
		} else if (args[i].equals("-sim")) {
		    simMode=true;
		} else {
		    System.err.println("UNKNOWN OPTIONS '"+args[i]+"'!");
		    printHelp();
		    System.exit(1);
		}
	    }
	}

	if (susana && root>0)
	    System.err.println("WARNING: autosetting root to 0 since '-s' option were selected.");

	if (ascii || bv || asciiOffline || compressedGraph) {

	    if (!directCompression && !printTemp) {
		printTemp=true;
		System.err.println("WARNING: autosetting 'p' option (create temporary file '"+network+".parser') since 'x' command were not selected.");
	    }

	    System.out.println();
	    if (directCompression)
		System.out.println("Parsing-Compression phase.");
	    else
		System.out.println("Parsing phase.");
	    System.out.println();

	    BFSParser parser=null;
	    Graph graph=null;

	    if (ascii || asciiOffline || compressedGraph) {
		try {
		    System.out.print("Loading graph...");
		    if (ascii)
			graph=new ASCIIGraph(network,false);
		    else if (asciiOffline)
			graph=new ASCIIOfflineGraph(network);
		    else
			graph=new CompressedGraph(network);
		    System.out.println(" done.");
		    System.out.println();
		} catch (ASCIIGraphFormatException e) {
		    System.out.println();
		    System.err.println(e.getMessage());
		    System.exit(1);
		} catch (Exception e) {
		    System.out.println();
		    if (compressedGraph) System.err.println("Cannot read graph file '"+network+".gc'!");
		    else System.err.println("Cannot read graph file '"+network+".net'!");
		    System.err.println(e.getMessage());
		    System.exit(1);
		}

		try {
		    parser=new BFSParser(graph,directCompression,fast,stats,printTemp,mapFile,susana,simMode,System.out);
		} catch (Exception e) {
		    System.err.println("Cannot create graph parser for file '"+network+".net'!");
		    System.err.println(e.getMessage());
		    System.exit(1);
		}
	    } else { // bv
		BVGraphWrapper g=null;
		try {
		    System.out.print("Loading graph...");
		    g=new BVGraphWrapper(network);
		    System.out.println(" done.");
		    System.out.println();
		    graph=g;
		} catch (Exception e) {
		    System.out.println();
		    System.err.println("Cannot create BV graph for graph '"+network+"'!");
		    System.err.println(e.getMessage());
		    System.exit(1);
		}

		try {
		    parser=new BVParser(g,directCompression,fast,stats,printTemp,mapFile,susana,simMode,System.out);
		} catch (Exception e) {
		    System.err.println("Cannot create graph parser for BV graph '"+network+"'!");
		    System.err.println(e.getMessage());
		    System.exit(1);
		}
	    }

	    if (root>graph.getVertexCount()) {
		System.err.println("Wrong root node '"+root+
				   "'. It must be an integer less than the graph dimension "+graph.getVertexCount()+"!");
		System.exit(1);
	    }

	    if (root<0)
		root=new Random().nextInt(graph.getVertexCount());

	    System.out.println("Nodes: "+graph.getVertexCount()+" Links: "+graph.getEdgeCount()+
			       " Level: "+level+" Root: "+root);
	    System.out.println();

	    try {
		parser.evaluate(level,root);
	    } catch (Exception e) {
		System.out.println();
		System.err.println(e.getMessage());
		e.printStackTrace();
		System.exit(1);
	    }

	}

	if (compression) {
	    System.gc();System.gc();System.gc();

	    System.out.println();
	    System.out.println("Compression phase.");
	    System.out.println();

	    try {
		Compressor.evaluate(network,fast,simMode,System.out);
	    } catch (Exception e) {
		System.err.println("Error while compressing graph file '"+network+".parser'");
		System.err.println(e.getMessage());
		e.printStackTrace();
		System.exit(1);
	    }
	}
    }

}

