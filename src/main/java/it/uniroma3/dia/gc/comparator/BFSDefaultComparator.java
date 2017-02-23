package it.uniroma3.dia.gc.comparator;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>This class is the default <code>BFSComparator</code> used to perform the BFS.</p>
 *
 * @author  Guido Drovandi
 * @version 0.3.5
 * @since   0.3.5
 */
public class BFSDefaultComparator extends BFSComparator {

    private Map<Integer, Integer> metric1;
    private final Map<Integer, Integer> metric2;

    public BFSDefaultComparator() {
        this.metric2 = new HashMap<>();
    }

    @Override
    public void beforeSort(final Integer[] a, final int length) {
        this.metric1 = new HashMap<>(length);
    }

    @Override
    public void afterSort(final Integer[] a, final int length) {
    }

    @Override
    public void addToQueue(final int node) {
        final int[] outs = graph.getSuccessors(node);
        for (int o : outs) {
            if (metric2.containsKey(o)) {
                metric2.put(o, metric2.get(o) + 1);
            } else {
                metric2.put(o, 1);
            }
        }
    }

    @Override
    public void nodeBFS(int node) {
        final int[] outs = graph.getSuccessors(node);
        for (int n : outs) {
            metric2.put(n, metric2.get(n) - 1);
        }
    }

    @Override
    public int compare(final Integer n1, final Integer n2) {
        final int c1 = metric2.get(n1);
        final int c2 = metric2.get(n2);

        if (c2 < c1) {
            return -1;
        }
        if (c1 < c2) {
            return 1;
        }

        final int d1 = graph.outDegree(n1);
        final int d2 = graph.outDegree(n2);

        if (d1 == 0 && d2 == 0) {
            return graph.inDegree(n2) - graph.inDegree(n1);
        }

        int in1 = 0, in2 = 0;
        if (metric1.get(n1) == null) {
            final int[] outs1 = graph.getSuccessors(n1);
            for (int i = 0; i < d1; i++) {
                if (parser.getBFSValue(outs1[i]) != -1) {
                    in1++;
                }
            }
            metric1.put(n1, in1);
        } else {
            in1 = metric1.get(n1);
        }
        if (metric1.get(n2) == null) {
            final int[] outs2 = graph.getSuccessors(n2);
            for (int i = 0; i < d2; i++) {
                if (parser.getBFSValue(outs2[i]) != -1) {
                    in2++;
                }
            }
            metric1.put(n2, in2);
        } else {
            in2 = metric1.get(n2);
        }
        if (in1 < in2) {
            return -1;
        }
        if (in2 < in1) {
            return 1;
        }
        return graph.inDegree(n2) - graph.inDegree(n1);
    }

}
