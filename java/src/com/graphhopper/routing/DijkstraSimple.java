package com.graphhopper.routing;

import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.BitSet;
import java.util.PriorityQueue;

/**
 * Implements a single source shortest path algorithm
 * http://en.wikipedia.org/wiki/Dijkstra's_algorithm
 *
 * @author Peter Karich,
 */
public class DijkstraSimple extends AbstractRoutingAlgorithm {

    protected BitSet visited = new BitSet();
    private TIntObjectMap<EdgeEntry> map = new TIntObjectHashMap<EdgeEntry>();
    private PriorityQueue<EdgeEntry> heap = new PriorityQueue<EdgeEntry>();

    public DijkstraSimple(Graph graph) {
        super(graph);
    }

    @Override
    public Path calcPath(int from, int to) {
        EdgeEntry fromEntry = new EdgeEntry(EdgeIterator.NO_EDGE, from, 0d);
        visited.set(from);
        EdgeEntry currEdge = fromEntry;
        while (true) {
            int neighborNode = currEdge.endNode;
            EdgeIterator iter = neighbors(neighborNode);
            while (iter.next()) {
                int tmpNode = iter.node();
                if (visited.get(tmpNode))
                    continue;

                double tmpWeight = iter.distance() + currEdge.weight;
                EdgeEntry nEdge = map.get(tmpNode);
                if (nEdge == null) {
                    nEdge = new EdgeEntry(iter.edge(), tmpNode, tmpWeight);
                    nEdge.parent = currEdge;
                    map.put(tmpNode, nEdge);
                    heap.add(nEdge);
                } else if (nEdge.weight > tmpWeight) {
                    heap.remove(nEdge);
                    nEdge.edge = iter.edge();
                    nEdge.weight = tmpWeight;
                    nEdge.parent = currEdge;
                    heap.add(nEdge);
                }

                updateShortest(nEdge, neighborNode);
            }

            visited.set(neighborNode);
            if (finished(currEdge, to))
                break;

            if (heap.isEmpty())
                return new Path();
            currEdge = heap.poll();
            if (currEdge == null)
                throw new AssertionError("cannot happen?");
        }

        if (currEdge.endNode != to)
            return new Path();

        return extractPath(currEdge);
    }

    protected boolean finished(EdgeEntry currEdge, int to) {
        return currEdge.endNode == to;
    }

    public Path extractPath(EdgeEntry goalEdge) {
        return new Path(graph).edgeEntry(goalEdge).extract();
    }
    
    protected EdgeIterator neighbors(int neighborNode) {
        return graph.getOutgoing(neighborNode);
    }
}