package com.sixsimplex.treasurehunt.revelocore.util.constants;

public class AppConstants {

    public static final String METADATA = "metadata";
    public static final int EXIT_FROM_MAP = 37;
    public static String TRAIL_TABLE_NAME = "trails",
            TRAIL_OLD_TABLE_TABLENAME = "usertrail",
            TRAIL_TABLE_TRAILID = "trailid",
            TRAIL_TABLE_STARTTIMESTAMP = "starttimestamp",
            TRAIL_TABLE_ENDTIMESTAMP = "endtimestamp",
            TRAIL_TABLE_ISNEW = "isnew",
            TRAIL_TABLE_LABEL = "Trail",
            TRAIL_TABLE_TRANSPORT_MODE = "transportmode",
            TRAIL_TABLE_USERNAME = "username",
            TRAIL_TABLE_W9_METADATA = "w9metadata",
            TRAIL_TABLE_W9_ENTITY_CLASS_NAME = "w9entityclassname",
            TRAIL_TABLE_DISTANCE = "distance",
            TRAIL_TABLE_JURISDICTION_INFO = "jurisdictioninfo",
            TRAIL_TABLE_DESCRIPTION = "description";
    public static String REGP_FILE = "regp";
    public static String METADATA_FILE = "revelometadata";
    public static String DATA_GP_FILE = "reveloentities";

    public static String CLIENT_ID = "reveloadmin";
    public static String GRANT_TYPE = "password";
    public static String SUCCESS = "success";
    public static String STATUS = "status";
    public static String ATTACHMENT_COUNT = "attachmentCount";

    public static final String GIS_SERVER_URL = "gisServerUrl";

    public static String CONTENT_TYPE_APPLICATION_JSON = "application/json";
    public static String ERROR = "error";
    public static String ERROR_DESCRIPTION = "error_description";
    public static String ERROR_MESSAGE = "message";

    public static final int NETWORK_TIME_OUT_MS = 120000;//2 min

    public static final int DOWNLOAD_DATA_REQUEST = 2;
    public static final int LOGIN_DATA_REQUEST = 1;
    public static final int SHOW_SURVEY_LIST = 3;
    public static final int CREATE_DATA_DB_REQUEST = 4;
    public static final int CREATE_METADATA_DB_REQUEST = 5;
    public static final int DATABASE_FILE_EXIST = 6;
    public static final int SHOW_JURISDICTION_DIALOG = 7;
    public static final int CREATE_META_GP_FILE_REQUEST = 71;
    public static final int REFRESH_DATA_REQUEST = 8;
    public static final int CHANGE_JURISDICTION_REQUEST = 9;
    public static final int CHANGE_SURVEY_REQUEST = 10;
    public static final int CHANGE_SURVEY_PHASE_REQUEST = 101;
    public static final int EXIT_REQUEST = 11;
    public static final int LOGOUT_REQUEST = 12;
    public static final int ERROR_UPLOAD_AVAILABLE = 1201;
    public static final int ERROR_WHEREAMI_ON = 1202;
    public static final int ERROR_TRAIL_ON = 1203;
    public static final int ERROR_NO_NETWORK = 1204;
    public static final int LOGOUT_RETRY = 13;
    public static final int LOGOUT_UPLOAD_DATA_AVAILABLE_RETRY = 133;
    public static final int OK_REQUEST = 14;

    public static final int CHANGE_BASEMAP_REQUEST = 15;
    public static final int MEASUREMENT_REQUEST = 16;
    public static final int GO_TO_JURISDICTIONS_REQUEST = 17;

    public static final int MEASUREMENT_AREA_REQUEST = 18;
    public static final int MEASUREMENT_DISTANCE_REQUEST = 19;
    public static final int MEASUREMENT_COORDINATES_REQUEST = 20;

    public static final int DELETE_FEATURE_REQUEST = 21;

    public static final int UPLOAD_REQUEST = 22;
    public static final int LOCATION_REQUEST = 23;
    public static final int RETURN_HOME_AFTER_LOADING = 24;
    public static final int MAP_LOADED = 35;
    public static final int EDIT_FEATURE_FORM_REQUEST = 26;
    public static final int START_WHERE_AM_I =27;
    public static final int WMS_SETTING_CHANGED = 271;
    public static final int ON_BACK_OPERATION_FEATURE_DRAWING =28;
    public static final int ON_BACK_OPERATION_ADD_EDIT_FRAGMENT =35;
    public static final int ON_BACK_OPERATION_ADD_EDIT_FRAGMENT_IN_BASE =36;

    public static final int ERROR_GETTING_OBCM =50;

    public static final int RQUESTCODE_ADD_EDIT_DELETE_FEATURE =51;
    public static final int ERRORCODE_ADD_EDIT_DELETE_NO_PERMISSION =52;



//    public static final int ERROR_PRINCIPLE = 51;
//    public static final int ERROR_SURVEY = 52;
//    public static final int ERROR_METADATA = 52;
//    public static final int ERROR_METADATA = 52;

    public static final String FAILURE = "failure";

    public static final String FAILURE_MESSAGE = "failureMessage";

    public static final String appFolderName = "Revelo 3.0";

    public static final String CREATE_DATA_GP_FILE = "createDataGpFile";
    public static final String CREATE_META_GP_FILE = "createMetaGFile";

    public static final String MULTIPOLYGON = "MultiPolygon";
    public static final String POLYGON = "Polygon";
    public static final String MULTILINESTRING = "MultiLineString";
    public static final String POLYLINE = "Polyline";
    public static final String POINT = "Point";

    public static final String EDIT = "edit";
    public static final String SHADOW = "shadow";
    public static final String ADD = "add";
    public static final String DELETE = "delete";
    public static final String label = "label";
    public static final String LOG = "log";
    public static final String ATTACHMENT = "attachment";
    public static final String SAVE_ONLY = "save";
    public static final String SAVE_APPROVE = "save_approve";
    public static final String SAVE_DISAPPROVE = "save_disapprove";



    public static final String video_path = "videoPath";
    public static final String video_name = "videoName";

    public static final String image_path = "imagePath";
    public static final String image_name = "imageName";

    public static final String photo = "Photo";
    public static final String video = "Video";
    public static final String audio = "Audio";

    public static final String videoType = "video/mp4";
    public static final String imageType = "image/png";
    public static final String audioType = "audio/mpeg";

    public static final String videoExtension = ".mp4";
    public static final String imageExtension = ".png";
    public static final String audioExtension = ".mp3";

    public static final int IMAGE_REQUEST = 123;
    public static final int VIDEO_REQUEST = 456;
    public static final int AUDIO_REQUEST = 789;

    public static final String attachmentType = "attachmentType";
    public static final String attachmentPath = "attachmentPath";
    public static final String attachmentFile = "attachmentFile";

    public static final String UPLOAD_FEATURE_ENTITY_LIST = "entityList";
    public static final String UPLOAD_ENTITY_ID_LIST = "entityIdList";

    public static final String W9_METADATA = "w9metadata";
    public static final String W9_ENTITY_CLASS_NAME = "w9entityclassname";

    public static final String W9_LATITUDE = "w9latitude";
    public static final String W9_LONGITUDE = "w9longitude";
    public static final String W9_ACCURACY = "w9accuracy";

    public static final String W9_UPDATE_DATE = "w9updatedate";
    public static final String W9_UPDATE_BY = "w9updatedby";

//    public static final String W9_ATTACHMENTS_INFO = "w9attachmentsinfo";

    public static final String COORDINATES = "coordinates";
    public static final String _ID = "_id";
    public static final String _TYPE = "_type";
    public static final String VERTEX = "vertex";

    public static final String ON_NEW_INTENT="onNewIntent";
    public static final String ON_CREATE_INTENT="onCreateIntent";

    public static final String caption = "caption";
}

