package com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

public interface IReveloDataWriter {

    //start region Vector and Table Data

    /**
     * Updates content of a dataset
     *
     * @throws Exception
     */
    public JSONObject updateDatasetContent(JSONObject datasetInfo, JSONArray dataJSONArray);

    /**
     * Writes content into a dataset
     *
     * @throws Exception
     */
    public JSONObject writeDatasetContent(Context context, JSONObject datasourceInfo, JSONObject datasetInfo, JSONArray dataJSONArray);

    /**
     * @param datasetInfo
     * @return
     */
    public JSONObject emptyDataset(JSONObject datasetInfo);

    /**
     * Deletes content
     *
     * @param //datasetName
     * @param //dataJSONArray
     * @return
     * @throws Exception
     */
    public JSONObject deleteDatasetContent(JSONObject datasourceInfo, JSONObject datasetInfo,Context context);
    //end region  Vector and Table Data

    /**
     * Cleans up resources allocated for data writing. Throws specified exception.
     *
     * @param exception
     */
    public void cleanUp(Exception exception);
}
