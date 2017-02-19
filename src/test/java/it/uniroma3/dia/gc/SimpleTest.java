package it.uniroma3.dia.gc;

import it.uniroma3.dia.gc.algorithms.PageRank;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Guido Drovandi
 * @version 0.1
 */
public class SimpleTest extends TestCase {

    public SimpleTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        new File("simple.gc").deleteOnExit();
        new File("simple.info").deleteOnExit();
        new File("simple.map").deleteOnExit();
    }

    public void testMain() throws Exception {
        System.out.println("main");
        String[] args = {"ax", "simple", "-map"};
        Main.main(args);
        args = new String[]{"simple", "100", "0.2"};
        PageRank.main(args);
        //args = new String[]{"simple"};
        //CompressedGraph.main(args);
    }

}
