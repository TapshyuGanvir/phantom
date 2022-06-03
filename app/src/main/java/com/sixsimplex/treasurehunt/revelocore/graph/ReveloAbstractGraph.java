package com.sixsimplex.treasurehunt.revelocore.graph;

import org.jgrapht.graph.DirectedAcyclicGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;
import org.jgrapht.traverse.TopologicalOrderIterator;
import org.json.JSONObject;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class ReveloAbstractGraph<V, E> {

    protected DirectedAcyclicGraph<V, E> nativeGraph = null;

    public ReveloAbstractGraph(Class<E> edgeClass) {
        this.nativeGraph = new DirectedAcyclicGraph<V, E>(edgeClass);
    }

    public JSONObject toJson() {
        JSONObject graphJSON = new JSONObject();
        return graphJSON;
    }

    @Override
    public String toString() {
        return this.toJson().toString();
    }

    //start vertices
    public int inDegreeOf(V vertex) {
        return this.nativeGraph.inDegreeOf(vertex);
    }

    public int outDegreeOf(V vertex) {
        return this.nativeGraph.outDegreeOf(vertex);
    }

    public boolean addVertex(V vertex) {
        if (!this.nativeGraph.containsVertex(vertex)) {
            return this.nativeGraph.addVertex(vertex);
        }
        return false;
    }

    public V getEdgeSource(E edge) {
        return this.nativeGraph.getEdgeSource(edge);
    }

    public V getEdgeTarget(E edge) {
        return this.nativeGraph.getEdgeTarget(edge);
    }

    public Set<V> getRootVertices() {
        Set<V> rootVerticesSet = new HashSet<>();
        Set<V> verticesSet = this.nativeGraph.vertexSet();
        Iterator<V> verticesIt = verticesSet.iterator();
        while (verticesIt.hasNext()) {
            V vertex = verticesIt.next();
            if (this.nativeGraph.inDegreeOf(vertex) == 0) {
                rootVerticesSet.add(vertex);
            }
        }
        return rootVerticesSet;
    }

    public boolean isRootVertex(V vertex) {
        if (this.nativeGraph.inDegreeOf(vertex) == 0) {
            return true;
        }
        return false;
    }

    public Set<V> getLeafVertices() {
        Set<V> leafVerticesSet = new HashSet<>();
        Set<V> verticesSet = this.nativeGraph.vertexSet();
        Iterator<V> verticesIt = verticesSet.iterator();
        while (verticesIt.hasNext()) {
            V vertex = verticesIt.next();
            if (this.nativeGraph.outDegreeOf(vertex) == 0) {
                leafVerticesSet.add(vertex);
            }
        }
        return leafVerticesSet;
    }

    public boolean isLeafVertex(V vertex) {
        if (this.nativeGraph.outDegreeOf(vertex) == 0) {
            return true;
        }
        return false;
    }

    public Set<V> getAllVertices() {
        return this.nativeGraph.vertexSet();
    }

    public Set<V> getAncestors(V vertex) {
        return this.nativeGraph.getAncestors(vertex);
    }

    public Map<String, V> getAncestorNodesMap(V childVertex, String idFieldName) {
        return new HashMap<String, V>();
    }

    public Set<V> getDescendants(V vertex) {
        return this.nativeGraph.getDescendants(vertex);
    }

    public Map<String, V> getDescendantsNodesMap(V childVertex, String idFieldName) {
        return new HashMap<String, V>();
    }

    public V getParent(V childVertex) {
        if (this.nativeGraph.containsVertex(childVertex) && this.nativeGraph.inDegreeOf(childVertex) > 0) {
            Set<E> incomingEdges = this.nativeGraph.incomingEdgesOf(childVertex);
            Iterator<E> itEdges = incomingEdges.iterator();

            //since JSON graphs will always one incoming edge
            if (itEdges.hasNext()) {
                return this.nativeGraph.getEdgeSource(itEdges.next());
            }
        }
        return null;
    }

    public List<V> getChildren(V parentVertex) {
        List<V> childrenVertices = new ArrayList<>();
        if (this.nativeGraph.containsVertex(parentVertex) && this.nativeGraph.outDegreeOf(parentVertex) > 0) {
            Set<E> outgoingEdges = this.nativeGraph.outgoingEdgesOf(parentVertex);
            Iterator<E> itEdges = outgoingEdges.iterator();
            while (itEdges.hasNext()) {
                childrenVertices.add(this.nativeGraph.getEdgeTarget(itEdges.next()));
            }
        }
        return childrenVertices;
    }

    public List<V> getVertices(Map<String, Object> propertiesMap) {
        List<V> verticesList = new ArrayList<V>();
        Set<V> allVertices = this.nativeGraph.vertexSet();
        Iterator<V> itVertices = allVertices.iterator();
        while (itVertices.hasNext()) {
            V node = itVertices.next();
            verticesList.add(node);
        }
        return verticesList;
    }

    public boolean deleteVertex(V vertex) {
        if (this.nativeGraph.containsVertex(vertex)) {
            return this.nativeGraph.removeVertex(vertex);
        }
        return false;
    }
    public boolean updateVertex(V vertex,V targetVertex) {
        return false;
    }
    //end vertices

    //start edges
    public void addEdge(V sourceVertex, V targetVertex, JSONObject properties) {
        this.nativeGraph.addEdge(sourceVertex, targetVertex);
    }

    public void addEdge(V sourceVertex, V targetVertex, JSONObject properties,String fromNodeName, String ToNodeName) {
        this.nativeGraph.addEdge(sourceVertex, targetVertex);
    }

    public Set<E> getAllEdges() {
        return this.nativeGraph.edgeSet();
    }

    public Set<E> outgoingEdgesOf(V vertex) {
        return this.nativeGraph.outgoingEdgesOf(vertex);
    }

    public Set<E> incomingEdgesOf(V vertex) {
        return this.nativeGraph.incomingEdgesOf(vertex);
    }

    public E getEdgeBetween(V sourceVertex, V targetVertex) {
        Set<E> set = this.nativeGraph.getAllEdges(sourceVertex, targetVertex);
        if (!set.isEmpty()) {
            return set.iterator().next();
        }
        return null;
    }

    public boolean deleteEdge(E edge) {
        return this.nativeGraph.removeEdge(edge);
    }
    //end edges

    //start algos

    /**
     * Returns a BFS iterator
     *
     * @return
     */
    public BreadthFirstIterator<V, E> getBFSIterator() {
        return new BreadthFirstIterator<>(this.nativeGraph);
    }

    /**
     * Returns a DFS iterator
     *
     * @return
     */
    public DepthFirstIterator<V, E> getDFSIterator() {
        return new DepthFirstIterator<>(this.nativeGraph);
    }

    /**
     * Returns a topo iterator
     *
     * @return
     */
    public TopologicalOrderIterator<V, E> getTopoSortIterator() {
        return new TopologicalOrderIterator<>(this.nativeGraph);
    }
    /**
     * Returns a topo iterator
     *
     * @return
     */
    public TopologicalOrderIterator<V, E> getTopoSortIterator(Comparator<V> comparator) {
        return new TopologicalOrderIterator<>(this.nativeGraph,comparator);
    }

    /**
     * Returns an iterator that iterates in reverse BFS
     *
     * @return
     */
    public Iterator<V> getReverseBFSIterator() {
        /*
         * Get BFS iterator and push all iterated elements into a Stack.
         * Then pop and use.
         *
         */
        ArrayDeque<V> stack = new ArrayDeque<V>();

        Iterator<V> verticesIt = this.getBFSIterator();
        while (verticesIt.hasNext()) {
            V vertex = verticesIt.next();
            stack.push(vertex);
        }
        return stack.iterator();
    }

    /**
     * @param vertex
     * @param orderedSet - orderedSet = new LinkedHashSet<V>();
     */
    public void getOrderedAncestors(V vertex, Set<V> orderedSet) {
        V parentVertex = this.getParent(vertex);
        if (parentVertex != null) {
            orderedSet.add(parentVertex);
            this.getOrderedAncestors(parentVertex, orderedSet);
        }
    }

    /**
     * @param starterVertex
     * @return
     */
    public ClosestFirstIterator<V, E> getClosestFirstIterator(V starterVertex, int radius) {
        return new ClosestFirstIterator<>(this.nativeGraph, starterVertex, radius);
    }

    /**
     * Use the BFS Iterator and call getDepth(V) on each visited vertex. Arrange the nodes by level afterwards.
     * WARNING - Returns wrong level numbers, due to seemingly wrong BFS traversal result.
     *
     * @return
     * @throws //JSONException
     */
    public Map<Integer, List<V>> getNodesByLevel() {
        Map<Integer, List<V>> levelMap = new HashMap<Integer, List<V>>();
        BreadthFirstIterator<V, E> breadthFirstIterator = new BreadthFirstIterator<>(this.nativeGraph);

        //iterate all vertices and place them in a list
        List<V> visitedVerticesList = new ArrayList<V>();
        while (breadthFirstIterator.hasNext()) {
            V vertex = breadthFirstIterator.next();
            visitedVerticesList.add(vertex);
        }

        //iterator through the list now
        Iterator<V> itVisitedList = visitedVerticesList.iterator();
        while (itVisitedList.hasNext()) {
            V vertex = itVisitedList.next();
            Integer level = breadthFirstIterator.getDepth(vertex);

            List<V> levelList = new ArrayList<V>();
            if (levelMap.containsKey(level)) {
                levelList = levelMap.get(level);
            } else {
                levelMap.put(level, levelList);
            }
            levelList.add(vertex);

        }

        return levelMap;
    }

    /**
     * Use the BFS Iterator and call getDepth(V) on vertex.
     * WARNING - Returns wrong level numbers, due to seemingly wrong BFS traversal result as in method getnodesbylevel.
     *
     * @return
     * @throws //JSONException
     */
    public Integer getLevelOfNode(V inputVertex) {
        Map<Integer, List<V>> levelMap = new HashMap<Integer, List<V>>();
        BreadthFirstIterator<V, E> breadthFirstIterator = new BreadthFirstIterator<>(this.nativeGraph);

        //iterate all vertices and place them in a list
        LinkedList<V> visitedVerticesList = new LinkedList<>();
        while (breadthFirstIterator.hasNext()) {
            V vertex = breadthFirstIterator.next();
            visitedVerticesList.add(vertex);
        }

        //iterator through the list now
        Integer level =-1;
        Iterator<V> itVisitedList = visitedVerticesList.iterator();
        while (itVisitedList.hasNext()) {
            V vertex = itVisitedList.next();
            if(inputVertex.equals(vertex)) {
                 level = breadthFirstIterator.getDepth(vertex);
            }
        }

        return level;
    }

    /**
     * Detects cycles
     * @return
     */
//	public boolean hasCycles() {
//		CycleDetector<V, E> cycleDetector = new CycleDetector<V, E>(this.nativeGraph);
//		return cycleDetector.detectCycles();
//	}
    //end algos
}
