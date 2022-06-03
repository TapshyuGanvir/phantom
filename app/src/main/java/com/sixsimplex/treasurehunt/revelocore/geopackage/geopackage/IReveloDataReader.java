package com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage;

import android.content.Context;

import org.json.JSONObject;

public interface IReveloDataReader {

    public boolean datasetExists(Context context, JSONObject dataSourceInfo, JSONObject datasetInfo);

    /**
     *
     * @return
     */
    public JSONObject getBaseUrl();

    /**
     * Returns only the dataset header and not data
     * @param datasetInfo
     * @return
     */
    public JSONObject getDatasetHeader(JSONObject datasetInfo);

    /**
     * Returns dataset header and data as JSON
     * @param //dataSource
     * @param //dataset
     * @return
     */
    public JSONObject getDatasetContent(Context context,JSONObject dataSourceInfo,JSONObject datasetInfo);

    /**
     * Returns metadata for all or specified datasets in the datasource as a JSONArray
     * @param //dataSource
     * @return
     */
    public JSONObject getMetadata(JSONObject datasetInfo);

    /**
     * Returns the dataset as a file. For spatial datasets, it returns a shapefile wrapped up into a zip file.
     * For table datasets, it returns an excel file.
     * @return
     * @throws Exception
     */
    public JSONObject getAsFile(JSONObject datasetInfo);

    /**
     * Returns a list and some basic info of all datasets or objects in the physical data source
     * @return
     */
    public JSONObject getAllObjectsList(JSONObject datasourceInfo);

    /**
     * Cleans up resources allocated for data reading. Throws specified exception.
     * @param exception
     */
    public void cleanUp(Exception exception);
}
