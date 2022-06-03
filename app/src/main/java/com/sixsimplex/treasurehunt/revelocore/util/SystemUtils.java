package com.sixsimplex.treasurehunt.revelocore.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;

import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMEntity;
import com.sixsimplex.treasurehunt.revelocore.conceptModel.CMUtils;
import com.sixsimplex.treasurehunt.revelocore.geopackage.models.RelationShipModel;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMEdge;
import com.sixsimplex.treasurehunt.revelocore.graph.concepmodelgraph.CMGraph;
import com.sixsimplex.treasurehunt.revelocore.layer.FeatureLayer;
import com.sixsimplex.treasurehunt.revelocore.util.constants.GraphConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.progressdialog.PercentageProgressBar;
import com.tinkerpop.blueprints.Graph;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SystemUtils {
private  static Graph DMGraph = null;

    public static boolean unzip(Context context, File zipFile, String destinationFolderPath, final PercentageProgressBar progressDialog) {

        try {

            InputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(new BufferedInputStream(fin));
            ZipEntry ze;
            while(( ze = zin.getNextEntry()) != null) {
//                if(progressDialog!=null) {
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(progressDialog!=null) {
//                                progressDialog.setTitle("Preparing data...");
//                                progressDialog.setDownloadMax(100);
//                                progressDialog.setDownloadProgress(0);
//                                progressDialog.setDownloadSpeed(- 1);
//                                progressDialog.setDownloadSize(- 1, "");
//                            }
//                        }
//                    });
//                }
                File file = new File(destinationFolderPath + File.separator + ze.getName());
                String canonicalPath = file.getCanonicalPath();
                if (!canonicalPath.startsWith(destinationFolderPath)) {
                    // SecurityException
                    throw new Exception(String.format("Found Zip Path Traversal Vulnerability with %s", canonicalPath));
                }else {
                    FileOutputStream fout = new FileOutputStream(file);//new File(path, ze.getName()));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    final long fileLength = ze.getSize();
                    long total = 0;
                    int progress;
                    byte[] buffer = new byte[1024];
                    int count;

                    // reading and writing
                    while ((count = zin.read(buffer)) != -1) {

                        baos.write(buffer, 0, count);
                        byte[] bytes = baos.toByteArray();
                        fout.write(bytes);
                        baos.reset();

                        total += count;
                        progress = (int) (total * 100 / fileLength);
//                        if(progressDialog!=null) {
//                            progressDialog.setDownloadProgress(progress);
//                        }
                    }
                    fout.close();
                    zin.closeEntry();

                }
                // Finish unzipping…
            }
            zin.close();
            return true;

           /* InputStream fin = new FileInputStream(zipFile);

            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {

                if(progressDialog!=null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setTitle("Preparing data...");
                            progressDialog.setDownloadMax(100);
                            progressDialog.setDownloadProgress(0);
                            progressDialog.setDownloadSpeed(-1);
                            progressDialog.setDownloadSize(-1, "");
                        }
                    });
                }

                File file = new File(destinationFolderPath + File.separator + ze.getName());

                if (ze.isDirectory()) {
                    file.mkdirs();
                } else {

                    FileOutputStream fout = new FileOutputStream(file);//new File(path, ze.getName()));
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    final long fileLength = ze.getSize();
                    long total = 0;
                    int progress;
                    byte[] buffer = new byte[1024];
                    int count;

                    // reading and writing
                    while ((count = zin.read(buffer)) != -1) {

                        baos.write(buffer, 0, count);
                        byte[] bytes = baos.toByteArray();
                        fout.write(bytes);
                        baos.reset();

                        total += count;
                        progress = (int) (total * 100 / fileLength);
                        if(progressDialog!=null) {
                            progressDialog.setDownloadProgress(progress);
                        }
                    }
                    fout.close();
                    zin.closeEntry();
                }
            }

            zin.close();
            return true;*/

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    public static void copyTilesFile(Context context) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open("google_tiles_hybrid_dir.zip");
            OutputStream out = new FileOutputStream(AppFolderStructure.getMapTilesFolderPath(context)+"/"+"google_tiles_hybrid_dir.zip");
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
        } catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
    }

    public static HashMap<String,Object> getChildrenFeatureswhereclause(Activity activity,String currentEntityName, String parentEntityName, Object parentFeatureId){

        try {
            JSONObject graphResult = CMUtils.getCMGraph(activity);
            CMGraph cmGraph=null;
            if (graphResult.has("status") && graphResult.getString("status").equalsIgnoreCase("success")) {
                cmGraph = (CMGraph) graphResult.get("result");
            } else {
                ReveloLogger.error("SystemUtils", "getChildrenFeatureswhereclause", "Could not create entities list - could not fetch graph from memory. Reason - " + graphResult.getString("message"));
            }

            if(cmGraph==null){
                return null;
            }
            Map<String,Object> condtionmap  = new HashMap<>();
            condtionmap.put("name",parentEntityName);
            List<CMEntity> cmEntityList = cmGraph.getVertices(condtionmap);
            if(cmEntityList == null || cmEntityList.size() != 1) {
                return null;
            }
            CMEntity selectedEntity =  cmEntityList.get(0);
            List<CMEntity> childrenEntityList = cmGraph.getChildren(selectedEntity);
            for(CMEntity childEntity:childrenEntityList){
                if(childEntity.getName().equalsIgnoreCase(currentEntityName)) {
                    CMEdge edge = cmGraph.getEdgeBetween(selectedEntity, childEntity);
                    if (edge != null) {
                        String toIdCol = edge.getToParameterName();
                        String w9IdpropertyId="";
                        if (parentFeatureId instanceof String) {
                            w9IdpropertyId = " = '" + parentFeatureId + "'";
                        } else {
                            w9IdpropertyId = " = " + parentFeatureId;
                        }
                        /*String whereClause = toIdCol + w9IdpropertyId;
                        return whereClause;*/
                        HashMap<String, Object> conditionMap = new HashMap<>();
                        conditionMap.put(toIdCol,parentFeatureId);
                        return conditionMap;
                    }
                    break;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ReveloLogger.error("SystemUtils", "getChildrenFeatureswhereclause",
                    "Could not where clause");
        }




        /*List<RelationShipModel> relationShipModelList = RelationShipTable.getAllRelations(activity);
        Map<String, FeatureLayer> featureLayerMap = EntitiesTable.getFeatureLayers(activity);
        FeatureLayer featureLayer = featureLayerMap.get(currentEntityName);

        for (RelationShipModel relationShipModel : relationShipModelList) {

            String parentRelationName = relationShipModel.getFromCol();
            String ChildRelationName = relationShipModel.getToCol();

            if (parentRelationName.equalsIgnoreCase(parentEntityName) &&
                    ChildRelationName.equalsIgnoreCase(currentEntityName)) {
                String toIdCol = relationShipModel.getToIdCol();
                FeatureTable childFeatureTable = featureLayer.getFeatureTable();

                String w9IdpropertyId="";
                if (parentFeatureId instanceof String) {
                    w9IdpropertyId = " = '" + parentFeatureId + "'";
                } else {
                    w9IdpropertyId = " = " + parentFeatureId;
                }

                String whereClause = toIdCol + w9IdpropertyId;
                String w9IdPropertyName = featureLayer.getW9IdProperty();
                return whereClause;
            }
        }*/
        return null;
    }

    public static Drawable convertDrawableToGrayScale(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Drawable res = drawable.mutate();
        res.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
        return res;
    }

   /* public static void createZipFile(String sourcePath, String toLocation, boolean moveAllFilesToRoot) {
        final int BUFFER = 2048;

        File sourceFile = new File(sourcePath);
        try {
            BufferedInputStream origin = null;
            File file = new File(toLocation);
            if (file.exists()) {
                FileOutputStream dest = new FileOutputStream(toLocation);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                if (sourceFile.isDirectory()) {
                    zipSubFolder(out, sourceFile, sourceFile.getParent().length(), moveAllFilesToRoot);
                } else {
                    byte[] data = new byte[BUFFER];
                    FileInputStream fi = new FileInputStream(sourcePath);
                    origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(getLastPathComponent(sourcePath));
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    public static long getStorageAvailableMemoryInByte() {

        //memory in byte
        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = 0;
        try {
            bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bytesAvailable;
    }

    static double  computeSignedArea(List<GeoPoint> path, double radius) {
        int size = path.size();
        if (size < 3) {
            return 0.0D;
        } else {
            double total = 0.0D;
            GeoPoint prev = (GeoPoint) path.get(size - 1);
            double prevTanLat = Math.tan((1.5707963267948966D - Math.toRadians(prev.getLatitude())) / 2.0D);
            double prevLng = Math.toRadians(prev.getLongitude());

            double lng;
            for (Iterator var11 = path.iterator(); var11.hasNext(); prevLng = lng) {
                GeoPoint point = (GeoPoint) var11.next();
                double tanLat = Math.tan((1.5707963267948966D - Math.toRadians(point.getLatitude())) / 2.0D);
                lng = Math.toRadians(point.getLongitude());
                total += polarTriangleArea(tanLat, lng, prevTanLat, prevLng);
                prevTanLat = tanLat;
            }

            return total * radius * radius;
        }
    }

    private static double polarTriangleArea(double tan1, double lng1, double tan2, double lng2) {
        double deltaLng = lng1 - lng2;
        double t = tan1 * tan2;
        return 2.0D * Math.atan2(t * Math.sin(deltaLng), 1.0D + t * Math.cos(deltaLng));
    }

    public static double computeArea(List<GeoPoint> path) {
        return Math.abs(computeSignedArea(path));
    }

    public static double computeSignedArea(List<GeoPoint> path) {
        return computeSignedArea(path, 6371009.0D);
    }
    
  /*  public static Graph getDMGraph(Context context){
        if(DMGraph==null){
            Map<String, FeatureLayer> featureLayerMap = EntitiesTable.getFeatureLayers(context);

            List<RelationShipModel> relationShipModelList = RelationShipTable.getAllRelations(context);

            DMGraph = getEntityGraph(featureLayerMap, relationShipModelList);

        }
        return DMGraph;
    }*/
    private static Graph getEntityGraph(Map<String, FeatureLayer> featureLayerList, List<RelationShipModel> relationList) {

        try {
            JSONArray vertexArray = new JSONArray();
            JSONArray edgesArray = new JSONArray();

            JSONObject entitiesJsonGraph = new JSONObject();

            entitiesJsonGraph.put(GraphConstants.DIRECTED, false);
            entitiesJsonGraph.put(GraphConstants.ENTITIES_TYPE, false);
            entitiesJsonGraph.put(GraphConstants.MODE, "NORMAL");

            for (FeatureLayer entity : featureLayerList.values()) {

                JSONObject featureLayerJsonObject = new JSONObject();

                featureLayerJsonObject.put(GraphConstants._ID, entity.getName());
                featureLayerJsonObject.put(GraphConstants.NAME, entity.getName());
                featureLayerJsonObject.put(GraphConstants._TYPE, "vertex");
                featureLayerJsonObject.put(GraphConstants.LABEL, entity.getLabel());
                featureLayerJsonObject.put(GraphConstants.TYPE, entity.getType());
                featureLayerJsonObject.put(GraphConstants.GEOMETRY_TYPE, entity.getGeometryType());
                featureLayerJsonObject.put(GraphConstants.W9_ID_PROPERTY, entity.getW9IdProperty());
                featureLayerJsonObject.put(GraphConstants.W9_ID_LABEL, entity.getLabelPropertyName());
                featureLayerJsonObject.put(GraphConstants.CATEGORY_PROPERTY_NAME, entity.getCategoryPropertyName());
                featureLayerJsonObject.put(GraphConstants.ABBR, entity.getAbbr());
                featureLayerJsonObject.put(GraphConstants.IS_LOCKED, entity.isLocked());
                featureLayerJsonObject.put(GraphConstants.SELECTED_RENDERER_NAME, entity.getSelectedRendererName());

                vertexArray.put(featureLayerJsonObject);
            }

            for (RelationShipModel relationShip : relationList) {

                String name = relationShip.getName();
                String fromCol = relationShip.getFromCol();
                String toCol = relationShip.getToCol();
                String fromIdCol = relationShip.getFromIdCol();
                String toIdCol = relationShip.getToIdCol();

                JSONObject relationJsonObject = new JSONObject();

                relationJsonObject.put(GraphConstants._ID, name);
                relationJsonObject.put(GraphConstants.NAME, name);
                relationJsonObject.put(GraphConstants._LABEL, name);
                relationJsonObject.put(GraphConstants.DESCRIPTION, name);
                relationJsonObject.put(GraphConstants.TYPE, "1-M");
                relationJsonObject.put(GraphConstants._TYPE, "edge");

                relationJsonObject.put(GraphConstants.FROM_ID, fromIdCol);
                relationJsonObject.put(GraphConstants.TO_ID, toIdCol);

                relationJsonObject.put(GraphConstants.FROM, fromCol);
                relationJsonObject.put(GraphConstants.TO, toCol);
                relationJsonObject.put(GraphConstants._IN_V, toCol);
                relationJsonObject.put(GraphConstants._OUT_V, fromCol);

                edgesArray.put(relationJsonObject);
            }

            entitiesJsonGraph.put(GraphConstants.VERTICES, vertexArray);
            entitiesJsonGraph.put(GraphConstants.EDGES, edgesArray);

            return TinkerGraphUtil.convertJSONToGraph(entitiesJsonGraph);

        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error("SystemUtils", "getEntityGraph", e.getMessage());
        }

        return null;
    }

    /**
     * Logs error and returns it bundled in the response JSON
     *
     * @param message
     * @return
     */
    public static JSONObject logAndReturnErrorMessage(String message, Exception exception) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String augmentedMessage = message;
            if (exception != null) {
                exception.printStackTrace();
                augmentedMessage += "Exception: " + exception.getMessage();
            }
            responseJSON.put("message", augmentedMessage);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    /*public static JSONObject returnErrorMessageInJson(String message, Exception exception) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", "failure");

            String augmentedMessage = message;
            if (exception != null) {
                exception.printStackTrace();
                augmentedMessage += "Exception: " + exception.getMessage();
            }
            responseJSON.put("result", augmentedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return responseJSON;
    }*/

    /**
     * @param message
     * @return
     */
    public static JSONObject logAndReturnMessage(String status,String message) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", status);
            responseJSON.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    public static JSONObject logAndReturnObject(String status,String message, Object resultObject) {

        JSONObject responseJSON = new JSONObject();
        try {
            responseJSON.put("status", status);
            responseJSON.put("message", message);
            responseJSON.put("result", resultObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return responseJSON;
    }

    public static String getCurrentDateTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            return sdf.format(getDate());
    }
    public static String getCurrentDateTimeMiliSec() {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.getDefault());
            return sdf.format(getDate());
    }

    public static Date getDate() {
        return new Date();
    }

    public interface OkDialogBox {
        void onOkClicked(DialogInterface alertDialog);
    }

    public static void showOkDialogBox(String message, Activity activity) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);


        alertDialogBuilder.setMessage(message).setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        try {
            alertDialog.setCancelable(false);
            alertDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void showOkDialogBoxWithCallback(String message, Activity activity, final OkDialogBox okDialogBox) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        okDialogBox.onOkClicked(dialog);
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
