package com.sixsimplex.treasurehunt.Phantom1.direction;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Directions {


    Context context;
    String url;
    Location from;
    Location to;
    dResponse.ErrorListener errorListener;
    dResponse.Listener<JSONObject> listener;

    /**
     * Creates a new request with the given method.
     *
     * @param url           URL to fetch the string at
     * @param listener      Listener to receive the String response
     * @param errorListener Error listener, or null to ignore errors
     */
    public Directions(Context context, String url, Location from, Location to, dResponse.Listener<JSONObject> listener, dResponse.ErrorListener errorListener) {
        this.context = context;
        this.url = url;
        this.from = from;
        this.to = to;
        this.errorListener = errorListener;
        this.listener = listener;
    }

    public static class getOpenRouteDirections extends Directions {
        public getOpenRouteDirections(Context context, String url, Location from, Location to, dResponse.Listener<JSONObject> listener, dResponse.ErrorListener errorListener) {
            super(context, url, from, to, listener, errorListener);
            getDirection();
        }
        private void getDirection() {
            try {
                JSONObject bodyJson = new JSONObject();
                bodyJson.put("geometry", "true");
                bodyJson.put("units", "km");
                bodyJson.put("roundabout_exits", "true");
                bodyJson.put("language", "en-us");
                bodyJson.put("instructions_format", "text");
                bodyJson.put("instructions", "true");
                JSONArray coordinatesArray = new JSONArray();
                JSONArray fromLocationArray = new JSONArray();
                JSONArray toLocationArray = new JSONArray();
                fromLocationArray.put(from.getLongitude());
                fromLocationArray.put(from.getLatitude());
                toLocationArray.put(to.getLongitude());
                toLocationArray.put(to.getLatitude());
                coordinatesArray.put(fromLocationArray);
                coordinatesArray.put(toLocationArray);
                bodyJson.put("coordinates", coordinatesArray);


                try {
                    String requestBody = null;
                    RequestQueue queue = Volley.newRequestQueue(context);
                    if (bodyJson != null) {
                        requestBody = bodyJson.toString();
                    }
                    Uri uri = Uri.parse(url);
                    String finalRequestBody = requestBody;
                    StringRequest jsonObjRequest = new StringRequest(Request.Method.POST, uri.toString(), new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("response", response);
                            try {
                                JSONObject directionDataJSON = new JSONObject(response);
                                listener.onSuccess(directionDataJSON);
                            } catch (Exception e) {
                                errorListener.onErrorResponse(e.getMessage()+"\n"+e.getLocalizedMessage()+"\n");
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Log.i("error", error.getLocalizedMessage());
                            try {
                                errorListener.onErrorResponse(String.valueOf(error.networkResponse)+"\n"+error.getMessage()+"\n"+error.getLocalizedMessage());
                            } catch (Exception e) {
                                errorListener.onErrorResponse(e.getMessage()+"\n"+e.getLocalizedMessage()+"\n");
                                e.printStackTrace();
                            }

                        }
                    }) {
                        @Override
                        public String getBodyContentType() {
                            return "application/json; charset=utf-8";//set here instead
                        }

                        @Override
                        public byte[] getBody() {
                            try {
                                return finalRequestBody == null ? null : finalRequestBody.getBytes(StandardCharsets.UTF_8);
                            } catch (Exception uee) {
                                return null;
                            }
                        }

                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> params = new HashMap<>();
                            params.put("Content-Type", "application/json; charset=utf-8");
                            return params;
                        }

                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("start", from.toString());
                            params.put("end", to.toString());
                            return params;
                        }
                    };
                    jsonObjRequest.setRetryPolicy(new DefaultRetryPolicy(1500000, 1, 1));
                    queue.add(jsonObjRequest);
                } catch (Exception e) {
                    errorListener.onErrorResponse(e.getMessage()+"\n"+e.getLocalizedMessage()+"\n");
                    String errorMsg = "Failed to create data Base" + e.getMessage();
                    e.printStackTrace();
                }
            } catch (Exception e) {
                errorListener.onErrorResponse(e.getMessage()+"\n"+e.getLocalizedMessage()+"\n");
                e.printStackTrace();
            }
        }
    }
}
