package com.sixsimplex.treasurehunt.revelocore.selectJurisdiction;

import android.content.Context;
import android.os.AsyncTask;

import com.sixsimplex.treasurehunt.revelocore.geopackage.tableUtil.ReDbTable;
import com.tinkerpop.blueprints.Vertex;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

public class SelectJurisdictionDialogAsync extends AsyncTask<Boolean, Object, Object> {

    private Vertex rootVertex;
    private String idProperty,idPropertyDataType, assignedJurisdictionName, assignedJurisdictionType, nextSpinnerName, spinnerColumnName, value, requiredColumnName;
    private boolean isUpperNeed;
    private ISelectJurisdictionValues iSelectJurisdictionValues;
    private WeakReference<Context> contextWeakReference;

    public SelectJurisdictionDialogAsync(Vertex rootVertex, String idProperty, String idPropertyDataType,String assignedJurisdictionName, String assignedJurisdictionType,
                                         Context context, ISelectJurisdictionValues iSelectJurisdictionValues) {
        this.rootVertex = rootVertex;
        this.idProperty = idProperty;
        this.idPropertyDataType=idPropertyDataType;
        this.assignedJurisdictionName = assignedJurisdictionName;
        this.assignedJurisdictionType = assignedJurisdictionType;
        this.iSelectJurisdictionValues = iSelectJurisdictionValues;

        contextWeakReference = new WeakReference<>(context);
    }

    public SelectJurisdictionDialogAsync(String nextSpinnerName, String spinnerColumnName, String value,
                                         String requiredColumnName, Context context,
                                         ISelectJurisdictionValues iSelectJurisdictionValues) {
        this.nextSpinnerName = nextSpinnerName;
        this.spinnerColumnName = spinnerColumnName;
        this.value = value;
        this.requiredColumnName = requiredColumnName;
        this.iSelectJurisdictionValues = iSelectJurisdictionValues;

        contextWeakReference = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Boolean... objects) {

        isUpperNeed = objects[0];

        try {
            if (isUpperNeed) {
                return ReDbTable.upperHierarchyMap(rootVertex, idProperty, idPropertyDataType, assignedJurisdictionName, assignedJurisdictionType, contextWeakReference.get());
            } else {
                return ReDbTable.getEntityValues(spinnerColumnName, value, requiredColumnName, contextWeakReference.get());
            }
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(Object object) {
        super.onPostExecute(object);
        String msg = "Could not get data of Organization's Administrative boundaries while creating database. Please contact admin.";
        if (isUpperNeed) {
            if (object != null) {
                Map<String, Object> userUpperHierarchyMap = (Map<String, Object>) object;
                iSelectJurisdictionValues.j_spinnerUpperHierarchy(userUpperHierarchyMap, rootVertex);
            }else {
                iSelectJurisdictionValues.j_onErrorSelectingJurisdiction(msg);
            }
        } else {
            if (object != null) {
                List<String> valueList = (List<String>) object;
                iSelectJurisdictionValues.j_spinnerLowerHierarchy(valueList,nextSpinnerName);
            }else {
                iSelectJurisdictionValues.j_onErrorSelectingJurisdiction(msg);
            }
        }
    }
}
