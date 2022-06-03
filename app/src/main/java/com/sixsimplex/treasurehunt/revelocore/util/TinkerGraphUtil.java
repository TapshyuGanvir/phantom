package com.sixsimplex.treasurehunt.revelocore.util;

import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReader;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TinkerGraphUtil {


    public static Graph convertJSONToGraph(JSONObject graphJSON) {
        Graph surveyDMGraph = new TinkerGraph();
        try {
            InputStream is = new ByteArrayInputStream(graphJSON.toString().getBytes());
            GraphSONReader.inputGraph(surveyDMGraph, is);
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return surveyDMGraph;
    }

    public static List<Vertex> getRootVertices(Graph graph) {
        List<Vertex> rootVertices = new ArrayList<>();

        Map<Vertex, Long> vertexMap = new HashMap<>();
        for (Vertex vertex : graph.getVertices()) {
            VertexQuery query = vertex.query().direction(Direction.IN);
            long numIncomingEdges = query.count();

            vertexMap.put(vertex, numIncomingEdges);
        }

        //find out the entity with count = 0
        Set<Map.Entry<Vertex, Long>> entrySet = vertexMap.entrySet();
        for (Map.Entry<Vertex, Long> entry : entrySet) {
            if (entry.getValue() == 0) {
                Vertex rootVertex = entry.getKey();
                rootVertices.add(rootVertex);
            }
        }

        return rootVertices;
    }

    public  static boolean isRootVertex(Vertex vertex){
        VertexQuery query = vertex.query().direction(Direction.IN);
        long numIncomingEdges = query.count();
        if(numIncomingEdges==0)
            return true;
        else return false;
    }

    public static Vertex findRootVertex(Graph graph) {

        Map<Vertex, Long> vertexMap = new HashMap<>();
        for (Vertex vertex : graph.getVertices()) {
            VertexQuery query = vertex.query().direction(Direction.IN);
            long numIncomingEdges = query.count();

            vertexMap.put(vertex, numIncomingEdges);
        }

        //find out the entity with count = 0
        Set<Map.Entry<Vertex, Long>> entrySet = vertexMap.entrySet();
        Iterator<Map.Entry<Vertex, Long>> itEntries = entrySet.iterator();
        Vertex rootVertex = null;
        while (itEntries.hasNext()) {
            Map.Entry<Vertex, Long> entry = itEntries.next();
            if (entry.getValue() == 0) {
                rootVertex = entry.getKey();
                break;
            }
        }

        return rootVertex;
    }

    public static Map<String, String> getChildrenTypesMap(Vertex rootVertex) {
        Map<String, String> map = new HashMap<>();
        VertexQuery query = rootVertex.query().direction(Direction.OUT);
        Iterable<Vertex> iterableVertices = query.vertices();
        for (Vertex vertex : iterableVertices) {
            map.put((String) vertex.getProperty("name"), (String) vertex.getProperty("type"));
        }
        return map;
    }

    public static Map<String, String> getChildrenLabelMap(Vertex rootVertex) {
        Map<String, String> map = new HashMap<>();
        VertexQuery query = rootVertex.query().direction(Direction.OUT);
        Iterable<Vertex> iterableVertices = query.vertices();
        for (Vertex vertex : iterableVertices) {
            map.put((String) vertex.getProperty("label"), (String) vertex.getProperty("name"));
        }
        return map;
    }

    public static ArrayList<String> getChildList(Vertex rootVertex) {
        ArrayList<String> childList = new ArrayList<>();
        VertexQuery query = rootVertex.query().direction(Direction.OUT);
        Iterable<Vertex> iterableVertices = query.vertices();
        for (Vertex vertex : iterableVertices) {
            Object o = vertex.getProperty("name");
            childList.add(String.valueOf(o));
        }
        return childList;
    }

    public static List<String> getParentsName(Vertex childVertex) {
        ArrayList<String> childList = new ArrayList<>();
        VertexQuery query = childVertex.query().direction(Direction.IN);
        Iterable<Vertex> iterableVertices = query.vertices();
        for (Vertex vertex : iterableVertices) {
            Object o = vertex.getProperty("name");
            childList.add(String.valueOf(o));
        }
        return childList;
    }

    public static List<String> getParentsNameLabels(Vertex childVertex) {
        ArrayList<String> childList = new ArrayList<>();
        VertexQuery query = childVertex.query().direction(Direction.IN);
        Iterable<Vertex> iterableVertices = query.vertices();
        for (Vertex vertex : iterableVertices) {
            Object o = vertex.getProperty(GraphConstants.LABEL);
            childList.add(String.valueOf(o));
        }
        return childList;
    }

    public static String getParentColumnNameInChildrenTable(Vertex childVertex) {

        Iterable<Edge> edges = childVertex.getEdges(Direction.IN);
        Iterator<Edge> iterator = edges.iterator();
        if (iterator.hasNext()) {
            Edge edge = iterator.next();
            return edge.getProperty("toId");

        }
        return null;
    }


    public static List<Vertex> getVerticesByProperty(Graph graph, String propertyName, String propertyValue) {
        List<Vertex> verticesList = new ArrayList<>();
        for (Vertex vertex : graph.getVertices(propertyName, propertyValue)) {
            verticesList.add(vertex);
        }
        return verticesList;
    }
}