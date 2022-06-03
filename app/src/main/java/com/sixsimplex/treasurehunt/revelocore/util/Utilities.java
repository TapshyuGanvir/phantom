package com.sixsimplex.treasurehunt.revelocore.util;


import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.sixsimplex.treasurehunt.BuildConfig;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;
import com.sixsimplex.treasurehunt.revelocore.data.GeoJsonUtils;
import com.sixsimplex.treasurehunt.revelocore.layer.GeometryEngine;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.util.GeometricShapeFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Utilities {
	public static List<Feature> getAllFeatureInBuffer(List<Feature> featureList, Geometry geofenceBuffer) {
		List<Feature> inRangeFeature=new ArrayList<>();
		boolean isContained = false;
		try{
			if(featureList != null && geofenceBuffer != null){
				if(!featureList.isEmpty()){
					for(Feature feature:featureList){
						Geometry geometry = GeoJsonUtils.convertToJTSGeometry(feature.getGeoJsonGeometry());
						if (geometry != null) {
							isContained = GeometryEngine.intersects(geometry, geofenceBuffer);
							if (isContained) {
								inRangeFeature.add(feature);
								isContained = false;
							}
						}
					}
				}
			}
		}catch (Exception e){
			e.printStackTrace();
		}
		return inRangeFeature;
	}

	public static class Screen{
		public static float screenWidth= Resources.getSystem().getDisplayMetrics().widthPixels;
		public static float screenHeight= Resources.getSystem().getDisplayMetrics().heightPixels;
	}

	public static JSONArray getLikeQueryClausesArray(String fieldName, String fieldType, Object value) {
		JSONArray clauses = new JSONArray();
		try {
			for (int i = 0; i < 4; i++) {
				String valueStr = String.valueOf(value);
				switch (i) {
					case 0:
						valueStr = valueStr;
						break;
					case 1:
						valueStr = "%" + valueStr;
						break;
					case 2:
						valueStr = valueStr + "%";
						break;
					case 3:
						valueStr = "%" + valueStr + "%";
						break;
				}
				JSONObject conditionJobj = new JSONObject();
				conditionJobj.put("conditionType", "attribute");
				conditionJobj.put("columnName", fieldName);
				conditionJobj.put("valueDataType", fieldType);
				conditionJobj.put("value", valueStr);
				conditionJobj.put("operator", "LIKE");
				clauses.put(conditionJobj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return clauses;
	}
	/**
	 * Function to convert milliseconds time to
	 * Timer Format
	 * Hours:Minutes:Seconds
	 * */
	public String milliSecondsToTimer(long milliseconds){
		String finalTimerString = "";
		String secondsString = "";
		
		// Convert total duration into time
		   int hours = (int)( milliseconds / (1000*60*60));
		   int minutes = (int)(milliseconds % (1000*60*60)) / (1000*60);
		   int seconds = (int) ((milliseconds % (1000*60*60)) % (1000*60) / 1000);
		   // Add hours if there
		   if(hours > 0){
			   finalTimerString = hours + ":";
		   }
		   
		   // Prepending 0 to seconds if it is one digit
		   if(seconds < 10){ 
			   secondsString = "0" + seconds;
		   }else{
			   secondsString = "" + seconds;}
		   
		   finalTimerString = finalTimerString + minutes + ":" + secondsString;
		
		// return timer string
		return finalTimerString;
	}
	
	/**
	 * Function to get Progress percentage
	 * @param currentDuration
	 * @param totalDuration
	 * */
	public int getProgressPercentage(long currentDuration, long totalDuration){
		Double percentage = (double) 0;
		
		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);
		
		// calculating percentage
		percentage =(((double)currentSeconds)/totalSeconds)*100;
		
		// return percentage
		return percentage.intValue();
	}

	/**
	 * Function to change progress to timer
	 * @param progress - 
	 * @param totalDuration
	 * returns current duration in milliseconds
	 * */
	public int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = (int) (totalDuration / 1000);
		currentDuration = (int) ((((double)progress) / 100) * totalDuration);
		
		// return current duration in milliseconds
		return currentDuration * 1000;
	}

	public static double metersToDecimalDegrees(double meters, double latitude) {
		return meters / (111.32 * 1000 * Math.cos(latitude * (Math.PI / 180)));
	}

	public static void customToast(Context context, String text) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.custom_toast, null);
		TextView textView = (TextView) layout.findViewById(R.id.text);
		textView.setText(text);
		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_LONG);
		toast.setView(layout);
		toast.show();


	}

	public static Geometry createCircle(double latitude, double longitude, final double distanceinMeters) {
		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(8);
		shapeFactory.setCentre(new Coordinate(longitude, latitude));
//        shapeFactory.setSize(distanceinMeters * 2);
		double size = (distanceinMeters * 0.00001) / 1.11;
		//double size= (distanceinMeters);
		shapeFactory.setSize(size*2);
		return shapeFactory.createCircle();
	}

//	public static boolean isWhereIAmServiceRunning(ActivityManager manager){
//		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//			if ("com.sixsimplex.revelo3.whereami.WhereAmIService".equals(service.service.getClassName())) {
//				return true;
//			}
//		}
//		return false;
//	}
	public static boolean isSendLocationToServerServiceRunning(ActivityManager manager){
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if ("com.sixsimplex.revelo3.liveLocationUpdate.SendLocationToServerService".equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}


	public static String getApkName(Context context) {
		/*String packageName = context.getPackageName();
		PackageManager pm = context.getPackageManager();
		try {
			ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
			String apk = ai.loadLabel(pm).toString();
			return apk;
		} catch (Exception x) {
			x.printStackTrace();
		}
		return null;*/
		String mVersionName, mVersionBuild;
		if (BuildConfig.VERSION_NAME.contains("debug")){
			mVersionName = "App_Name Software version: " + BuildConfig.VERSION_NAME.substring(0,11);
			mVersionBuild = "Build version: " + BuildConfig.VERSION_NAME.substring(12);
		}else {
			mVersionName = "App_Name Software version: " + BuildConfig.VERSION_NAME.substring(0, 13);
			mVersionBuild = "Build version: " + BuildConfig.VERSION_NAME.substring(14);
		}

		return "Build version: " + BuildConfig.VERSION_NAME;
	}
}
