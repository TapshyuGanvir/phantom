package com.sixsimplex.treasurehunt.revelocore.util;

import android.text.TextUtils;

import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UrlPreferenceUtility;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

public class UrlStore {

   // private static String realmName = "revelo3.0";

    public static String securityTokenUrl() {
        String securityServerIp = UrlPreferenceUtility.getSecurityServerIP();
        String realmName = UrlPreferenceUtility.getSecurityRealmName();
        if (TextUtils.isEmpty(securityServerIp) ||TextUtils.isEmpty(realmName)) {
            return "";
        } else {
            return securityServerIp + "/auth/realms/" + realmName + "/protocol/openid-connect/token";
        }
    }

    public static String principalUrl(String limitResponseTo, String firstTimeDownload, String reDBTimeStamp) {

        String principalUrl = "";

        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        if (!TextUtils.isEmpty(AppServerIp)) {

            principalUrl = AppServerIp + "/reveloadmin/revelo/access/principal/mobile?limitResponseTo="
                    + limitResponseTo + "&firstTimeDownload=" + firstTimeDownload;

            if (!TextUtils.isEmpty(reDBTimeStamp)) {
                principalUrl = principalUrl + "&reDBTimeStamp=" + reDBTimeStamp;
            }
        }

        return principalUrl;
    }

    public static String surveyUrl(String surveyName) {

        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        if (TextUtils.isEmpty(AppServerIp)) {
            return "";
        } else {
            return AppServerIp + "/reveloadmin/revelo/surveys/" + surveyName;
        }
    }

    public static String getOrgBoundaryConceptModelGraphUrl() {

        String conceptModelName = "w9obcm";
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        if (TextUtils.isEmpty(AppServerIp)) {
            return "";
        } else {
            return AppServerIp + "/reveloadmin/revelo/conceptmodels/" + conceptModelName + "?details=true";
        }
    }

    public static String getUserProfileUrl(String userName) {

        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        if (TextUtils.isEmpty(AppServerIp)) {
            return "";
        } else {
            return AppServerIp + "/reveloadmin/revelo/users/" + userName + "/profile";
        }
    }

   /* public static String getWMSLayerUrl(String baseUrl, String layer) {
        return baseUrl +
                "?service=WMS" +
                "&version=1.1.1" +
                "&request=GetMap" +
                "&" + layer +
                "&bbox=%f,%f,%f,%f" +
                "&width=256" +
                "&height=256" +
                "&srs=EPSG:4326" +
                "&format=image/png" +
                "&transparent=true";
    }*/

    public static String getREDatabaseUrl(String orgName) {
        if (TextUtils.isEmpty(UrlPreferenceUtility.getAppServerIp())) {
            return "";
        } else {
            return UrlPreferenceUtility.getAppServerIp() + "/reveloadmin/revelo/" + orgName + "/w9obre";
        }
    }

    public static String createMetaDatabaseUrl(String surveyName) {
        if (TextUtils.isEmpty(UrlPreferenceUtility.getAppServerIp())) {
            return "";
        } else {
            return UrlPreferenceUtility.getAppServerIp() + "/reveloadmin/revelo/geopackage/metadata?projectName=" + surveyName;
        }

    }

    public static String downloadMetaDatabaseUrl(String surveyName) {
        if (TextUtils.isEmpty(UrlPreferenceUtility.getAppServerIp())) {
            return "";
        } else {
            return UrlPreferenceUtility.getAppServerIp() + "/reveloadmin/revelo/geopackage?projectName=" + surveyName + "&dbFileName="+UserInfoPreferenceUtility.getMetatdataDbName();
        }
    }

    public static String createDatabaseDbUrl(String surveyName) {
        if (TextUtils.isEmpty(UrlPreferenceUtility.getAppServerIp())) {
            return "";
        } else {
            return UrlPreferenceUtility.getAppServerIp() + "/reveloadmin/revelo/geopackage/entities?projectName=" + surveyName;

            /*String phaseName = UserInfoPreferenceUtility.getSurveyPhaseName(surveyName);
            if(phaseName==null||phaseName.isEmpty()) {
                return UrlPreferenceUtility.getAppServerIp() + "/reveloadmin/revelo/geopackage/entities?projectName=" + surveyName;
            }else {
                return UrlPreferenceUtility.getAppServerIp() + "/reveloadmin/revelo/geopackage/entities?projectName=" + surveyName+"&phaseName="+phaseName;
            }*/
        }
    }

    public static String downloadDataGpUrl(String surveyName) {
        if (TextUtils.isEmpty(UrlPreferenceUtility.getAppServerIp())) {
            return "";
        } else {
            return UrlPreferenceUtility.getAppServerIp() + "/reveloadmin/revelo/geopackage?projectName=" + surveyName + "&dbFileName="+ UserInfoPreferenceUtility.getDataDbName();
        }
    }

    public static String helpUrl() {
//        return "http://6simplex.co.in/revelo/userhelp.html";
        return "http://6simplex.co.in";
    }

    public static String logoutSessionUrl() {
        String securityServerIp = UrlPreferenceUtility.getSecurityServerIP();
        String realmName = UrlPreferenceUtility.getSecurityRealmName();
        if (TextUtils.isEmpty(securityServerIp) ||TextUtils.isEmpty(realmName)) {
            return "";
        }  else {
            return UrlPreferenceUtility.getSecurityServerIP() + "/auth/realms/" + realmName + "/protocol/openid-connect/logout";
        }
    }

    public static String dataUploadUrl(String userName, String surveyName, String operationName) {
        //http://{{domain}}/reveloadmin/revelo/users/{{userName}}/data?opName=delete&ownerType=survey&ownerName=meclsurvey

        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        if (TextUtils.isEmpty(AppServerIp)) {
            return "";
        } else {
            return AppServerIp + "/reveloadmin/revelo/users/" + userName + "/data?opName=" + operationName + "&ownerType=survey&ownerName=" + surveyName;
        }
    }

    public static String addAttachmentUrl(String dataModelName, String surveyName, String entityName, Object featureId) {

        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        //http://{{domain}}/reveloadmin/revelo/conceptmodels/{{conceptModelName}}/entities/site/Bharat Nagar/attachments?ownerType=survey&ownerName={{surveyName}}

        String url = AppServerIp + "/reveloadmin/revelo/conceptmodels/" + dataModelName + "/entities/" + entityName + "/" + featureId + "/" +
                "attachments?ownerType=survey&ownerName=" + surveyName /*+ "&mode=zip"*/;

        url = url.replace(" ", "%20");

        return url;
    }

    public static String deleteAttachmentUrl(String dataModelName, String surveyName, String entityName, Object featureId) {

        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        String url = AppServerIp + "/reveloadmin/revelo/conceptmodels/" + dataModelName + "/entities/" + entityName + "/" + featureId + "/" +
                "attachments?ownerType=survey&ownerName=" + surveyName;

        url = url.replace(" ", "%20");

        return url;
    }

    public static String getLogUploadUrl(String userName) {
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        return AppServerIp + "/reveloadmin/revelo/users/" + userName + "/profile/mobilelogs";
    }

    public static String metadataUploadUrl( String conceptModelName, String layerName, String featureId) {

        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        if (TextUtils.isEmpty(AppServerIp)) {
            return "";
        } else {
            //http://{{domain}}/reveloadmin/revelo/conceptmodels/{{conceptModelName}}/entities/site/Bharat Nagar/metadata
            return AppServerIp + "/reveloadmin/revelo/conceptmodels/" +conceptModelName  +"/entities/"+layerName+"/"+featureId+"/metadata";
        }
    }

    public static String traversalUploadUrl( String userName,String surveyName, String conceptModelName, String layerName) {

        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        if (TextUtils.isEmpty(AppServerIp)) {
            return "";
        } else {
            //{{protocol}}://{{domain}}/reveloadmin/revelo/users/{{userName}}/profile/assignedsurveys/{{surveyName}}/asentities/customers
            return AppServerIp + "/reveloadmin/revelo/users/" +userName  +"/profile/assignedsurveys/"+surveyName+"/asentities/"+layerName;
        }
    }

    public static String getUpdateProfileUrl() {
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();
        return AppServerIp + "/reveloadmin/revelo/users/" + UserInfoPreferenceUtility.getUserName() + "/profile";
    }

    public static String getProfilePicUrl() {
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();
        return AppServerIp + "/reveloadmin/revelo/users/" + UserInfoPreferenceUtility.getUserName() + "/profile/picture";
    }

    public static String getOrgLogoUrl() {
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();
        return AppServerIp+"/reveloadmin/revelo/"+UserInfoPreferenceUtility.getOrgName()+"/logoFile";
    }

    public static String getWebSocketUrl() {
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();
        if(AppServerIp.startsWith("https"))
            return AppServerIp + "/reveloadmin/revelo/users/"+UserInfoPreferenceUtility.getUserName()+"/" + UserInfoPreferenceUtility.getUserLocationWSSName();
        else
            return AppServerIp + "/reveloadmin/revelo/users/"+UserInfoPreferenceUtility.getUserName()+"/" + UserInfoPreferenceUtility.getUserLocationWSName() ;
    }

    public static String getMessageSocketUrl() {
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();
        if(AppServerIp.startsWith("https")){
            AppServerIp = AppServerIp.replace("https","wss");
        }else {
            AppServerIp = AppServerIp.replace("http","ws");
        }
        return AppServerIp+"/reveloadmin/revelo/users/"+UserInfoPreferenceUtility.getUserName()+"messagews";
    }

    public static String getCreateMessageUrl() {
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        return AppServerIp+"/reveloadmin/revelo/messages";
    }

    public static String getGetAllMessagesUrl() {
        String AppServerIp = UrlPreferenceUtility.getAppServerIp();

        return AppServerIp+"/reveloadmin/revelo/messages?all=true";
    }
}