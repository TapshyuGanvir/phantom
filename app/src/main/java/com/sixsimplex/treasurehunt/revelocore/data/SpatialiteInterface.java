package com.sixsimplex.treasurehunt.revelocore.data;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import org.json.JSONObject;
import jsqlite.Callback;
import jsqlite.Constants;
import jsqlite.Database;

public class SpatialiteInterface {
    private static String className = "SpatialiteInterface";
    private static Database gdb;
    private static boolean isGdbOpen;
    public static  JSONObject runQuery(Context context,String filePath, String query, Callback callback) throws Exception {

        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("status", "failure");
            String errorMsg = "unknown error";

            try {

                gdb = openGdb(context,filePath);
                if (gdb != null) {
                    gdb.exec("BEGIN;", null);
                    gdb.exec(query, callback);
                    gdb.exec("COMMIT;", null);

                    jsonObject.put("status", "success");
                    jsonObject.put("message", callback);
                } else {
                    jsonObject.put("status", "failure");
                    jsonObject.put("message", "unable to open database");
                }
            } catch (Exception e) {
                errorMsg = e.getMessage();
                e.printStackTrace();
                ReveloLogger.error(className, "Run Query", e.getMessage());
                jsonObject.put("message", errorMsg);
            } finally {
                if (gdb != null)
                    gdb.close();
            }



        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "Run Query", e.getMessage());
        }
        return jsonObject;
    }

  /*  public static Database openGdb(Context context,String filepath) {
        try {

            int mode = Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE;
         if(gdb==null) {
             gdb = new Database();
         }
            gdb.open(filepath, mode);
            return gdb;

        } catch (Exception e) {
            ReveloLogger.error(className, "Open Gdb", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


    public static void closeGdb(Database database) {

            try {
                    database.close();
            } catch (Exception e) {
                ReveloLogger.error(className, "Close Gdb", "error closing gdb: " + e.getMessage());
                e.printStackTrace();
            }

    }*/

    public static Database openGdb(Context context, String filepath) throws Exception {
        try {
            if (!isGdbOpen) {
                if(gdb==null) {
                    gdb = new Database();
                }
                int mode = Constants.SQLITE_OPEN_READWRITE | Constants.SQLITE_OPEN_CREATE;
                gdb.open(filepath, mode);
                isGdbOpen = true;
            }
        } catch (Exception je) {
            ReveloLogger.error(className, "open Gdb", "error opening gdb: " + je.getMessage());
            isGdbOpen = false;
            throw je;
        }
        return gdb;
    }

    public void closeGdb() throws Exception {

        try {
            if (isGdbOpen) {
                gdb.close();
                isGdbOpen = false;
            }
        } catch (Exception e) {
            ReveloLogger.error(className, "close Gdb", "error closing gdb: " + e.getMessage());
            if (e.getMessage().contains("closed")) {
                isGdbOpen = false;
            } else {
                isGdbOpen = true;
            }

            throw e;
        }
    }
}
