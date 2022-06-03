package com.sixsimplex.treasurehunt.Phantom1.utils;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.graph.GraphFactory;
import com.sixsimplex.treasurehunt.Phantom1.traversalgraph.TraversalGraph;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import java.util.HashMap;

public class TraversalUtils {
    public static HashMap<String,TraversalGraph> traversalGraphHashMap = new HashMap<>();

    public static  TraversalGraph getEntityTraversalGraph(CMEntity cmEntity){
        if(traversalGraphHashMap.containsKey(cmEntity.getName())){
            return traversalGraphHashMap.get(cmEntity.getName());
        }

        String traversalGraphstr = TraversalPreferenceUtility.getTraversalGraphStr(cmEntity.getName());
        if(traversalGraphstr==null||traversalGraphstr.isEmpty()){
            traversalGraphstr = cmEntity.getTraversalGraphStr();
            if(traversalGraphstr==null||traversalGraphstr.isEmpty()){
                return null;
            }else {
                return setTraversalGraph(cmEntity.getName(), cmEntity.getTraversalGraphStr());
            }
        }
       return setTraversalGraph(cmEntity.getName(), traversalGraphstr);
    }

    public static TraversalGraph setTraversalGraph(String entityName,String traversalGraphStr) {
        JSONObject traversalInputJson = null;
        TraversalGraph traversalGraph = null;
        try{
           /* if(traversalGraphStr==null || traversalGraphStr.isEmpty()){
                //return;
                traversalGraphStr="{\"properties\":{\"visibility\":{\"comment\":\"if visibleByDefault = true, then show all the features on the map initially, else use visibility property name and value to decide to show the feature. This applies to all features in this entity.\",\"visibleByDefault\":true,\"visibilityPropertyName\":\"for ex: isVisible\",\"visibilityPropertyValue\":\"for ex: true or false\"},\"approval\":{\"comment\":\"if requiresApproval = true, then field user will communicate with Admin and Admin will update the approvalPropertyName property with value = 'approvalPropertyValue'. Server will communicate this edit to mobile using web sockets and mobile app UI will take cognizance of that and update itself. This applies to all features in this entity.\",\"required\":false,\"approvalPropertyName\":\"for ex: isApproved\",\"approvalPropertyValue\":\"for ex: true or false\"}},\"graph\":{\"nodes\":{\"1001\":{\"isSkipable\":true,\"isVisited\":false},\"1002\":{\"isSkipable\":true,\"isVisited\":false},\"1003\":{\"isSkipable\":true,\"isVisited\":false},\"1004\":{\"isSkipable\":true,\"isVisited\":false},\"1005\":{\"isSkipable\":true,\"isVisited\":false},\"1006\":{\"isSkipable\":true,\"isVisited\":false},\"1007\":{\"isSkipable\":true,\"isVisited\":false},\"1008\":{\"isSkipable\":true,\"isVisited\":false},\"1009\":{\"isSkipable\":true,\"isVisited\":false}},\"edges\":[{\"from\":\"1001\",\"to\":\"1003\"},{\"from\":\"1003\",\"to\":\"1005\"},{\"from\":\"1005\",\"to\":\"1007\"},{\"from\":\"1007\",\"to\":\"1009\"},{\"from\":\"1009\",\"to\":\"1008\"},{\"from\":\"1008\",\"to\":\"1006\"},{\"from\":\"1006\",\"to\":\"1004\"},{\"from\":\"1004\",\"to\":\"1002\"}]}}";
            }*/

            traversalInputJson = new JSONObject(StringEscapeUtils.unescapeJson(traversalGraphStr.trim()));

            traversalGraph = GraphFactory.createTraversalGraph(traversalInputJson,"w9id");
            if(traversalGraph!=null) {
                traversalGraphHashMap.put(entityName, traversalGraph);
                TraversalPreferenceUtility.storeTraversalGraph(entityName,traversalGraph.toString());
            }else {
                traversalGraphHashMap.remove(entityName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return traversalGraph;
    }
}
