# Graph Compression by BFS

The `Graph Compression by BFS` project helps you to compress your graphs/networks in an efficient way using just a simple BFS and some
more complex tricks. You can use the compressed version directly to query and navigate your original graph, so it is possible to deal with
huge networks in main memory. Overall it is suitable for the Web Graph since it is able to exploit all the redundacies tipical of this graph.

This is a very old project of mine based on an article published with Prof. Alberto Apostolico. The original webpage is on
[my university site](http://www.dia.uniroma3.it/~drovandi/software.php), here you find the same code of the last version (0.3.2) with very minor
useless changes.

Feel free to branch the project, modify it, and write something better than this. I don't think to have time to improve this software however,
you can contact me for any information (do not expect quick answers, sorry).

You can download the original paper from [MDPI](http://www.mdpi.com/1999-4893/2/3/1031).
Please use the following `BibTeX` entry if you would like to cite this software:
```
@Article{ad-gcbfs-09,
    AUTHOR  = {Apostolico, Alberto and Drovandi, Guido},
    TITLE   = {Graph Compression by BFS},
    JOURNAL = {Algorithms},
    VOLUME  = {2},
    YEAR    = {2009},
    NUMBER  = {3},
    PAGES   = {1031--1044},
    URL     = {http://www.mdpi.com/1999-4893/2/3/1031},
    ISSN    = {1999-4893},
    DOI     = {10.3390/a2031031}
}
```

## Requirements

Very simple requirements:

* Java 7+
* [WebGraph](http://webgraph.di.unimi.it/)

To compress by BFS, `WebGraph` is not necessary (but you need to delete a couple of classes and compile again).

Before this project, `WebGraph` was the only project (standard de facto) so I used it to test `GCbyBFS` since they provide also
[excellent datasets](http://law.di.unimi.it/datasets.php). The project by Paolo Boldi and Sebastiano Vigna is a great work, so I'm not planning
to remove `WebGraph` from the requirements.

*Note: The original project was compiled under Java 6 so if you need to run it in Java 6 just few (very simple) changes are required*

**IMPORTANT**

**The program was tested under Java 6 but from Java 7 the Java merge algorithm was changed.
This is causing some issue, please use the `java.util.Arrays.useLegacyMergeSort=true` option:**
```
java -Djava.util.Arrays.useLegacyMergeSort=true -Xmx4g -Xms512m [CLASSPATH] it.uniroma3.dia.gc.Main OPTIONS
```
**I will check the error and hopefully fixing it.**

## Installation

Download the jar file, set `WebGraph` into the classpath, and that's it.

## Usage

To get help just run `java it.uniroma3.dia.gc.Main` and you will get:
```
Usage:
  java it.uniroma3.dia.gc.Main COMMANDS GRAPH [OPTIONS]

COMMANDS:
  a - parse an ASCII graph file (GRAPH.net)
  o - parse an ASCII graph file offline (GRAPH.net)
  g - parse a compressed graph (GRAPH.gc)
  b - parse a BV graph file (GRAPH.graph)
  c - compression of a parsed graph file (GRAPH.parser)
  x - direct compression of a graph (does not create the temporary file)
  p - create the temporary file GRAPH.parser

OPTIONS:
  -l LEVEL   - set the compression level (default 1000)
  -r NODE    - set the root of the BFS (default random, not with -s)
  -map       - writes the map file
  -f         - faster compression
  -s         - use original ids (BFS does not relabel nodes)
  -sim       - simulation mode, do not write the compressed graph file (only with 'x' or 'c')
```

## Example

Create a file `simple.net` containing this ASCII graph:
```
5
0 1 2
1 0
2
3 2 4
4 1 3
```

Run the program:
```
java it.uniroma3.dia.gc.Main ax simple -l 8 -map
```

Here it is the output:
```
Parsing-Compression phase.

Loading graph... done.

Nodes: 5 Links: 7 Level: 1000 Root: 1

BFS Compression: 100% (Links removed: 3) in 0 m 0 s                 
Writing .map file... done.

Size: 69 bits (9,857 bits/link)
```

Now you have three more files:
* simple.gc - the compressed graph
* simple.info - some information on the graph
* simple.map - mapping of the new node names to the original

To test the compressed graph we can run PageRank:
```
java it.uniroma3.dia.gc.algorithms.PageRank simple 100 0.2
```

It works!
```
Loading Graph... done.
Time: 20 ms
Creating PageRank... done.
Time: 0 ms
Computing ranks... done.
Time: 6 ms - Time/Iteration:  0,06 ms
 1. Page:          1	Rank: 0,20992
 2. Page:          0	Rank: 0,20802
 3. Page:          2	Rank: 0,20802
 4. Page:          3	Rank: 0,18702
 5. Page:          4	Rank: 0,18702
 6. Page:          0	Rank: 0,00000
...
```

## Acknowledgement

I wish to thank Sebastiano Vigna and Susana Ladra González for their interesting feedback on this project.

## License

GraphCompressionByBFS is EUPL-licensed
