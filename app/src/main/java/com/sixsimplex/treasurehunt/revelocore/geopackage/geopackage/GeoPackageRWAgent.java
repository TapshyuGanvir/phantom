package com.sixsimplex.treasurehunt.revelocore.geopackage.geopackage;

import android.content.Context;


import com.sixsimplex.treasurehunt.revelocore.util.SystemUtils;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import mil.nga.geopackage.GeoPackage;
import mil.nga.geopackage.GeoPackageManager;

public final class GeoPackageRWAgent implements IReveloDataReader, IReveloDataWriter {

	private String className = "GeoPackageRWAgent";
	private JSONObject geoPackagePropertiesJSON = null;
	private ReveloLogger geopackageRWLogger = null;
	private GeoPackage dataGeoPackage= null;
	private static GeoPackageManager manager = null;
	public GeoPackageRWAgent(JSONObject geoPackagePropertiesJSON,
							 Context context) throws Exception {

		ReveloLogger logger = new ReveloLogger();
		GeoPackageRWAgent gp = new GeoPackageRWAgent(geoPackagePropertiesJSON, logger,context);
		

	}

	public GeoPackageRWAgent(JSONObject geoPackagePropertiesJSON, ReveloLogger logger, Context context) {
		this.geopackageRWLogger = logger;

		try {
			/*this.geoPackagePropertiesJSON = geoPackagePropertiesJSON;
			if(GeoPackageUtils.doesGeoPackageExist(this.geoPackagePropertiesJSON)) {
				Map<String, String> params = new HashMap<>();
				params.put("dbtype", "geopkg");
				params.put("database", this.geoPackagePropertiesJSON.getString("dbPath"));

				String dbPath =  this.geoPackagePropertiesJSON.getString("dbPath");
				File dataGpkgFile = new File(dbPath);
				dataGpkgFile.mkdirs();
				String dbName = this.geoPackagePropertiesJSON.getString("dbName");
				dbName = dbName.split(".gpkg")[0];

				dataGeoPackage = GeoPackageManagerAgent.getGeoPackage(context,dbName);

				if (this.dataGeoPackage != null) {
					this.geopackageRWLogger.trace(className,"Constructor","Connection to data GeoPackage successful.");
				}
				else {
					throw new RuntimeException("Geopackage at " + this.geoPackagePropertiesJSON.getString("dbPath") + " does not exist.");
				}
			}*/
		} catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	//start Reader methods

    //start Writer methods
	@Override
    public JSONObject updateDatasetContent(JSONObject datasetInfo, JSONArray dataJSONArray) {
        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");
            responseJSON.put("message", "Not implemented");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    @Override
    public JSONObject writeDatasetContent(Context context, JSONObject datasourceInfo, JSONObject datasetInfo, JSONArray dataJSONArray) {
        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");
            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");
            String w9IdPropertyName = datasetInfo.getString("w9IdPropertyName");
            responseJSON = GeoPackageUtils.insertFeatures(context, datasourceName, datasetName, datasetType, w9IdPropertyName, dataJSONArray, this.geopackageRWLogger);
            if (responseJSON.getString("status").equalsIgnoreCase("failure")) {
                return SystemUtils.logAndReturnErrorMessage(responseJSON.getString("message"), null);
            }
            responseJSON.put("status", "success");
        } catch (JSONException e) {
            e.printStackTrace();
            return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
        }

        return responseJSON;
    }
    @Override
	public boolean datasetExists(Context context,JSONObject dataSourceInfo,JSONObject datasetInfo) {
		try {
			String datasetName = datasetInfo.getString("datasetName");
			String datasourceName = datasetInfo.getString("datasourceName");

			return GeoPackageUtils.datasetExists(context, datasetName, datasourceName);
		} catch(Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
    public JSONObject emptyDataset(JSONObject datasetInfo) {
        return null;
    }

    @Override
    public JSONObject deleteDatasetContent(JSONObject datasourceInfo, JSONObject datasetInfo, Context context) {
        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");

            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");


            return GeoPackageUtils.deleteFeatures(datasourceName, datasetName, datasetType, null, "", this.geopackageRWLogger, context);

        } catch (JSONException e) {
            e.printStackTrace();
            return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
        }
    }    @Override
	public JSONObject getBaseUrl() {
		return null;
	}

    public JSONObject deleteDatasetContent(JSONObject datasourceInfo, JSONObject datasetInfo, JSONArray whereClauseArray, String ANDorOR, Context context) {
        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");

            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");


            return GeoPackageUtils.deleteFeatures(datasourceName, datasetName, datasetType, whereClauseArray, ANDorOR, this.geopackageRWLogger, context);
        } catch (JSONException e) {
            e.printStackTrace();
            return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
        }
    }

    public JSONObject deleteDatasetContent(JSONObject datasourceInfo, JSONObject datasetInfo, String whereClause, Context context) {
        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");

            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");


            return GeoPackageUtils.deleteFeatures(datasourceName, datasetName, datasetType, whereClause, this.geopackageRWLogger, context);
        } catch (JSONException e) {
            e.printStackTrace();
            return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
        }
    }    @Override
	public JSONObject getDatasetHeader(JSONObject datasetInfo) {
		/*try {
			JSONObject responseJSON = new JSONObject();
			responseJSON.put("status", "failure");
			
			String datasetName = datasetInfo.getString("datasetName");
			if(this.datasetExists(datasetInfo)) {
				SimpleFeatureSource simpleFeatureSource = this.dataStore.getFeatureSource(datasetName);
				if(simpleFeatureSource != null) {
					return PostGISDOReaderUtils.getSchemaAsJSON(simpleFeatureSource);
				}				
			}
			else {
				responseJSON.put("message", "The dataset " + datasetName + " not found in the geopackage. Does it really exist?");
			}
			return responseJSON;
		} catch(JSONException | IOException e) {
			e.printStackTrace();
			return SystemUtils.logAndReturnErrorMessage(e.getMessage(), e);
		}*/
		return null;
	}

    public int getDatasetItemCount(Context context, JSONObject datasourceInfo, JSONObject datasetInfo, JSONArray whereClauseConditionArray, String ANDorOR) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");

            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
            int count = GeoPackageUtils.getFeatureCount(context, datasourceName, datasetName, datasetType, whereClauseConditionArray, ANDorOR, this.geopackageRWLogger);
            //}
            return count;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public int getDatasetItemCountNEW(Context context, JSONObject datasourceInfo, JSONObject datasetInfo,
                                      JSONArray ORClausesArray, JSONArray ANDClausesArray, String ANDorOR) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");

            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");
            int count = GeoPackageUtils.getFeatureCountNEW(context, datasourceName, datasetName, datasetType,
                                                            ORClausesArray,  ANDClausesArray,  ANDorOR, this.geopackageRWLogger);
            return count;
        } catch (JSONException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public JSONObject getDatasetContent(Context context, JSONObject datasourceInfo, JSONObject datasetInfo, List<String> requiredColumnsList, HashMap<String, JSONObject> columnNameValueConditionMap, String ANDorOR, boolean isDistinct, int limit, boolean queryGeometry) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");

            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
            responseJSON = GeoPackageUtils.getFeatures(context, datasourceName, datasetName, datasetType, columnNameValueConditionMap, ANDorOR, requiredColumnsList, isDistinct, 0, limit, queryGeometry, this.geopackageRWLogger);
            //}
            return responseJSON;
        } catch (JSONException e) {
            e.printStackTrace();
            return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
        }
    }    @Override
	public JSONObject getAllObjectsList(JSONObject datasourceInfo) {
		JSONObject allObjectsListJSON = new JSONObject();
		/*JSONArray listArray = new JSONArray();
		try {
			allObjectsListJSON.put("status", "failure");

			List<Name> namesList = this.dataStore.getNames();
			Iterator<Name> itNamesList = namesList.iterator();
			while(itNamesList.hasNext()) {
				Name name = itNamesList.next();

				SimpleFeatureSource simpleFeatureSource = this.dataStore.getFeatureSource(name);
				
				JSONObject metadataJSON = new JSONObject();
				metadataJSON.put("numRecords", simpleFeatureSource.getFeatures().size());
				
				SimpleFeatureType featureType = simpleFeatureSource.getSchema();				
				CoordinateReferenceSystem crs = featureType.getCoordinateReferenceSystem();
				if(crs != null){
					metadataJSON.put("wkid", CRS.lookupEpsgCode(crs, true));
				}else{
					metadataJSON.put("wkid", -1);
				}
				
				GeometryDescriptor geometryDescriptor = featureType.getGeometryDescriptor();
				String datasetType = "table", subType = "";
				if(geometryDescriptor != null) {
					datasetType = "spatial";
					String geometryType = featureType.getGeometryDescriptor().getType().getBinding().toString();
					if(geometryType.startsWith("class org.locationtech.jts.geom")) {
						subType = geometryType.substring(geometryType.lastIndexOf(".") + 1, geometryType.length());
					}
				}
				
				JSONObject datasetJSON = new JSONObject();
				datasetJSON.put("name", name.getLocalPart());
				datasetJSON.put("type", datasetType);
				datasetJSON.put("subType", subType);
				datasetJSON.put("w9IdPropertyName", "");//there is no way we can determine this, so leaving it blank.
				
				URI source = this.dataStore.getInfo().getSource();
				if(source != null && source.getScheme().equalsIgnoreCase("file")){
					datasetJSON.put("format", "shapefile");	
				}
				else{
					datasetJSON.put("format", "dbtable");
				}
				
				//get attributes and populate properties
				JSONObject attributesResponseJSON = PostGISDOReaderUtils.getAttributes(simpleFeatureSource, this.geopackageRWLogger);
				if(attributesResponseJSON.getString("status").equalsIgnoreCase("success")){
					metadataJSON.put("properties", attributesResponseJSON.getJSONArray("schema"));
				}
				else{
					return SystemUtils.logAndReturnErrorMessage(attributesResponseJSON.getString("message"), null);
				}				
				datasetJSON.put("metadata", metadataJSON);
				
				listArray.put(datasetJSON);
			}
			
			allObjectsListJSON.put("list", listArray);
			allObjectsListJSON.put("status", "success");
		} catch(JSONException | IOException  e) {
			e.printStackTrace();
			return SystemUtils.logAndReturnErrorMessage(e.getMessage(), e);
		} */
		return allObjectsListJSON;
	}

    public JSONObject getDatasetContent(Context context, JSONObject datasourceInfo, JSONObject datasetInfo, List<String> requiredColumnsList, HashMap<String, JSONObject> columnNameValueConditionMap, String ANDorOR, boolean isDistinct, int limit, boolean queryGeometry, boolean transformGeometry) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");

            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
            responseJSON = GeoPackageUtils.getFeatures(context, datasourceName, datasetName, datasetType, columnNameValueConditionMap, ANDorOR, requiredColumnsList, isDistinct, 0, limit, queryGeometry, transformGeometry, this.geopackageRWLogger);
            //}
            return responseJSON;
        } catch (JSONException e) {
            e.printStackTrace();
            return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
        }
    }

    public JSONObject getDatasetContent(Context context, JSONObject datasourceInfo, JSONObject datasetInfo, List<String> requiredColumnsList, JSONArray whereclauseArray, String ANDorOR, JSONArray compulsoryConditionsArray, boolean isDistinct, int limit, boolean queryGeometry, boolean transformGeometry) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String datasourceName = datasourceInfo.getString("datasourceName");

            String datasetName = datasetInfo.getString("datasetName");
            String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
            responseJSON = GeoPackageUtils.getFeatures(context, datasourceName, datasetName, datasetType, whereclauseArray, ANDorOR, compulsoryConditionsArray, requiredColumnsList, isDistinct, 0, limit, queryGeometry, transformGeometry, this.geopackageRWLogger);
            //}
            return responseJSON;
        } catch (JSONException e) {
            e.printStackTrace();
            return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
        }
    }    /**
	 * Returns dataset data as GeoJSON
	 * 
	 * @param datasetInfo
	 * @return
	 */
	@Override
	public JSONObject getDatasetContent(Context context, JSONObject datasourceInfo , JSONObject datasetInfo) {
		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("status", "failure");
			String datasourceName = datasourceInfo.getString("datasourceName");

			String datasetName = datasetInfo.getString("datasetName");
			if(!this.datasetExists(context,datasourceInfo,datasetInfo)) {
				responseJSON.put("message", "The dataset " + datasetName + " not found in the geopackage. Does it really exist?");
				return responseJSON;
			}
			
			//construct required variables with sensible defaults
			String columnsList = null;
			JSONObject whereClauseJSON = new JSONObject();
			JSONArray clauses = new JSONArray();
			whereClauseJSON.put("clauses", clauses);
			whereClauseJSON.put("logicalOperator", "AND");
			
			if(datasetInfo.has("whereClause")) {
				whereClauseJSON = datasetInfo.getJSONObject("whereClause");
			}
			
			if(datasetInfo.has("columns")) {
				columnsList = datasetInfo.getString("columns");
			}
			
			//paging parameters
			int startIndex = -1, maxFeatures = -1;
			if(datasetInfo.has("startIndex")){
				startIndex = datasetInfo.getInt("startIndex");
			}
			
			if(datasetInfo.has("maxFeatures")){
				maxFeatures = datasetInfo.getInt("maxFeatures");
			}

			boolean returnGeometry = false;
			if(datasetInfo.has("returnGeometry")){
				returnGeometry = datasetInfo.getBoolean("returnGeometry");
			}

			/*if(!returnGeometry) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(datasourceName, datasetName, whereClauseJSON, columnsList, startIndex, maxFeatures, this.geopackageRWLogger);
			} else {
				responseJSON = GeoPackageUtils.getFeatures(context,datasourceName, datasetName,"spatial", whereClauseJSON, columnsList, false, startIndex, maxFeatures, this.geopackageRWLogger);
			}*/
		} catch(JSONException e) {
			e.printStackTrace();
			return SystemUtils.logAndReturnErrorMessage(e.getMessage(), e);
		}
		
		return responseJSON;
	}

    public JSONObject getDatasetContent(Context context, JSONObject datasourceInfo, JSONObject datasetInfo, List<String> requiredColumnsList, JSONArray whereclauseArray, String ANDorOR, boolean isDistinct, int limit, boolean queryGeometry, boolean transformGeometry) {

		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("status", "failure");

			String datasourceName = datasourceInfo.getString("datasourceName");

			String datasetName = datasetInfo.getString("datasetName");
			String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
            responseJSON = GeoPackageUtils.getFeatures(context, datasourceName, datasetName, datasetType, whereclauseArray, ANDorOR, requiredColumnsList, isDistinct, 0, limit, queryGeometry, transformGeometry, this.geopackageRWLogger);
			//}
			return responseJSON;
		} catch (JSONException e) {
			e.printStackTrace();
			return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
		}
	}

    public JSONObject getDatasetContent(Context context, JSONObject datasourceInfo, JSONObject datasetInfo, List<String> requiredColumnsList, HashMap<String, JSONObject> columnNameValueConditionMap, String ANDorOR, boolean isDistinct, String groupBy, String orderBy, boolean desc, int limit, boolean queryGeometry) {

		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("status", "failure");

			String datasourceName = datasourceInfo.getString("datasourceName");

			String datasetName = datasetInfo.getString("datasetName");
			String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
            responseJSON = GeoPackageUtils.getFeatures(context, datasourceName, datasetName, datasetType, columnNameValueConditionMap, ANDorOR, requiredColumnsList, isDistinct, groupBy, orderBy, desc, 0, limit, queryGeometry, this.geopackageRWLogger);
			//}
			return responseJSON;
		} catch (JSONException e) {
			e.printStackTrace();
			return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
		}
    }    @Override

    public JSONObject getMetadata(JSONObject datasetInfo) {
        return null;
	}

    public JSONObject getDatasetContentNEW(Context context, JSONObject datasourceInfo, JSONObject datasetInfo,
                                        List<String> requiredColumnsList, JSONArray ORClausesArray, JSONArray ANDClausesArray, String ANDorOR,
                                        boolean isDistinct, int startIndex,int limit, boolean queryGeometry, boolean transformGeometry) {

		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("status", "failure");

			String datasourceName = datasourceInfo.getString("datasourceName");

			String datasetName = datasetInfo.getString("datasetName");
			String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
            responseJSON = GeoPackageUtils.getFeaturesNEW(context, datasourceName, datasetName, datasetType,
                                                          requiredColumnsList,ORClausesArray,ANDClausesArray,ANDorOR,
                                                          isDistinct,startIndex,limit,queryGeometry,transformGeometry);
			//}
			return responseJSON;
		} catch (JSONException e) {
			e.printStackTrace();
			return SystemUtils.logAndReturnErrorMessage("Exception occured while writing dataset content. ", e);
		}
	}

    public int getDatasetItemCount(Context context, JSONObject datasourceInfo, JSONObject datasetInfo) {

		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("status", "failure");

			String datasourceName = datasourceInfo.getString("datasourceName");

			String datasetName = datasetInfo.getString("datasetName");
			String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
            int count = GeoPackageUtils.getFeatureCount(context, datasourceName, datasetName, datasetType, null, this.geopackageRWLogger);
			//}
			return count;
		} catch (JSONException e) {
			e.printStackTrace();
			return 0;
		}
    }    @Override

    public JSONObject getAsFile(JSONObject datasetInfo) {
        return null;
    }
    //end Reader methods

	public JSONObject getDatasetContent(Context activity, JSONObject dataSourceInfoForDataGpkg, JSONObject datasetInfo, String whereClause) {
		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("status", "failure");

			String datasourceName = dataSourceInfoForDataGpkg.getString("datasourceName");

			String datasetName = datasetInfo.getString("datasetName");
			String datasetType = datasetInfo.getString("datasetType");

			responseJSON= GeoPackageUtils.getFeatures(activity,datasourceName, datasetName,datasetType, whereClause, this.geopackageRWLogger);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return responseJSON;
	}

	public JSONObject updateDatasetContent(JSONObject datasourceInfo, JSONObject datasetInfo, JSONArray dataArray,
										JSONArray whereclauseArray,String ANDorOR ,Context context) {
		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("status", "failure");

			String datasourceName = datasourceInfo.getString("datasourceName");

			String datasetName = datasetInfo.getString("datasetName");
			String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
			return GeoPackageUtils.updateFeatures(context,datasourceName, datasetName,datasetType, dataArray,whereclauseArray, ANDorOR, this.geopackageRWLogger);
			//}
		} catch (JSONException e) {
			e.printStackTrace();
			ReveloLogger.error(className,"update dataset content","exception "+e.getMessage());
		}
		return responseJSON;
	}

	public JSONObject updateDatasetContent(JSONObject datasourceInfo, JSONObject datasetInfo, JSONArray dataArray,String whereClause ,Context context) {
		JSONObject responseJSON = new JSONObject();
		try {
			responseJSON.put("status", "failure");

			String datasourceName = datasourceInfo.getString("datasourceName");

			String datasetName = datasetInfo.getString("datasetName");
			String datasetType = datasetInfo.getString("datasetType");
			/*if(!datasetType.equalsIgnoreCase("spatial")) {
				responseJSON = GeoPackageUtils.getFeaturesWithoutGeometry(context,datasourceName, datasetName, columnNameValueConditionMap, requiredColumnsList, 0, limit, this.geopackageRWLogger);
			} else {*/
			responseJSON = GeoPackageUtils.updateFeatures(context,datasourceName, datasetName,datasetType, dataArray,whereClause, this.geopackageRWLogger);
			//}
		} catch (JSONException e) {
			e.printStackTrace();
			ReveloLogger.error(className,"update dataset content","exception "+e.getMessage());
			try{
				responseJSON.put("status", "Failure");
				responseJSON.put("message", "Update failed. Reason: exception- "+e.getMessage());
			}catch (Exception e1){
				e1.printStackTrace();
		}
	}
		return responseJSON;
	}







    //end writer methods



    /**
     * Cleans up allocated resources and throws specified exception
     *
     * @throws //IOException
     * @throws //AutomationException
     */
    public void cleanUp(Exception exception) {
		/*this.geopackageRWLogger.trace("Beginning clean up.");
		if(this.dataStore!= null) {
			this.dataStore.dispose();
			this.dataStore = null;
			this.geopackageRWLogger.trace("Data Store disposed off.");
		}

		if(exception != null) {
			String message = "Exception: " + exception.getMessage();
			this.geopackageRWLogger.error("Following exception occured during GeoPackageReaderAgent.cleanup(): " + message);
		}*/
    }








}
