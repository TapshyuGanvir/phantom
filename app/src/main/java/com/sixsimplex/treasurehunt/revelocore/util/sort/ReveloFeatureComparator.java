package com.sixsimplex.treasurehunt.revelocore.util.sort;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.Phantom1.traversalgraph.TraversalGraph;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONObject;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

public class ReveloFeatureComparator implements Comparator<Feature> {

    public static String AlphabeticalSort ="AlphabeticalSort";
    public static String NumericalSort ="NumericalSort";
    public static String TraversalSort ="TraversalSort";
    private String sortMode;
    private String sortKeyName;
    private CMEntity cmEntity;
    private static String className = "ReveloFeatureComparator";
    private TraversalGraph traversalGraph;

    public ReveloFeatureComparator(String sortMode, String sortPropertyName){
       this.sortMode=sortMode;
       this.sortKeyName=sortPropertyName;
      this.traversalGraph=null;
    }
    public ReveloFeatureComparator(String sortMode, String sortPropertyName,TraversalGraph traversalGraph){
       this.sortMode=sortMode;
       this.sortKeyName=sortPropertyName;
       this.traversalGraph=traversalGraph;
    }

    @Override
    public int compare(Feature f_lhs, Feature f_rhs) {
      String taskName = "compare";
        if(sortKeyName==null ||sortKeyName.isEmpty()){
            ReveloLogger.error(className,taskName,"sortKeyName not provided..returning 'equal'");
            return 0;
        }
        if(sortMode==null ||sortMode.isEmpty()){
            ReveloLogger.error(className,taskName,"sortmode not set properly..using alphabetical sorting mode");
            sortMode = AlphabeticalSort;
        }

//perform basic sanity
        if(f_lhs==null || f_rhs==null
        || f_lhs.getAttributes()==null||f_rhs.getAttributes()==null
        || !f_lhs.getAttributes().containsKey(sortKeyName) || f_lhs.getAttributes().get(sortKeyName)==null
        || !f_rhs.getAttributes().containsKey(sortKeyName)|| f_rhs.getAttributes().get(sortKeyName)==null){
            ReveloLogger.error(className,taskName,"either one of two input features are null or they dnt have value for "+sortKeyName+"..returning 'equal'");
            return 0;
        }


        if(sortMode.equalsIgnoreCase(AlphabeticalSort)){
            try {
                char l = Character.toUpperCase(String.valueOf(f_lhs.getAttributes().get(sortKeyName)).charAt(0));

                if (l < 'A' || l > 'Z')

                    l += 'Z';

                char r = Character.toUpperCase(String.valueOf(f_rhs.getAttributes().get(sortKeyName)).charAt(0));

                if (r < 'A' || r > 'Z')

                    r += 'Z';

                String s1 = l + String.valueOf(f_lhs.getAttributes().get(sortKeyName)).substring(1);

                String s2 = r + String.valueOf(f_rhs.getAttributes().get(sortKeyName)).substring(1);

                return s1.compareTo(s2);
            }catch (Exception e){
                ReveloLogger.error(className,taskName,"Exception while sorting alphabetically..returning 'equal'. Exception: "+e.getMessage());
                e.printStackTrace();
            }
        }
        else if(sortMode.equalsIgnoreCase(NumericalSort)){
            try {
                int l = Integer.parseInt(String.valueOf(f_lhs.getAttributes().get(sortKeyName)));

                int r = Integer.parseInt(String.valueOf(f_rhs.getAttributes().get(sortKeyName)));
                if (l == r)
                    return 0;
                if (l > r)
                    return 1;
                if (l < r)
                    return - 1;
            }catch (Exception e){
                ReveloLogger.error(className,taskName,"Exception while sorting numerically..returning 'equal'. Exception: "+e.getMessage());
                e.printStackTrace();
            }
        }
        else if(sortMode.equalsIgnoreCase(TraversalSort)){
           if(traversalGraph==null){
               return 0;
           }

            Map<String, Object> conditionMap = new HashMap<>();
            conditionMap.put("w9id", f_lhs.getFeatureId());
           List<JSONObject> vertexLhsList = traversalGraph.getVertices(conditionMap);
           if(vertexLhsList==null ||vertexLhsList.size()==0){
               return 0;
           }
           JSONObject vertexLHS = vertexLhsList.get(0);

           conditionMap.clear();
            conditionMap.put("w9id", f_rhs.getFeatureId());
            List<JSONObject> vertexRhsList = traversalGraph.getVertices(conditionMap);
            if(vertexRhsList==null ||vertexRhsList.size()==0){
                return 0;
            }
            JSONObject vertexRHS = vertexRhsList.get(0);

            try {
                int l = traversalGraph.getLevelOfNode(vertexLHS);

                int r = traversalGraph.getLevelOfNode(vertexRHS);
                if (l == r)
                    return 0;
                if (l > r)
                    return 1;
                if (l < r)
                    return - 1;
            }catch (Exception e){
                ReveloLogger.error(className,taskName,"Exception while sorting numerically..returning 'equal'. Exception: "+e.getMessage());
                e.printStackTrace();
            }

        }
        ReveloLogger.error(className,taskName,"sort mode "+sortMode+" not supported..returning 'equal'.");
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public Comparator reversed() {
        return null;
    }

    @Override
    public Comparator thenComparing(Comparator other) {
        return null;
    }

    @Override
    public Comparator thenComparingInt(ToIntFunction keyExtractor) {
        return null;
    }

    @Override
    public Comparator thenComparingLong(ToLongFunction keyExtractor) {
        return null;
    }

    @Override
    public Comparator thenComparingDouble(ToDoubleFunction keyExtractor) {
        return null;
    }

    @Override
    public Comparator thenComparing(Function keyExtractor) {
        return null;
    }

    @Override
    public Comparator thenComparing(Function keyExtractor, Comparator keyComparator) {
        return null;
    }
}