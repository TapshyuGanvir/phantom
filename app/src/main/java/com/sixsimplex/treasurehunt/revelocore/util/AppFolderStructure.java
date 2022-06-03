package com.sixsimplex.treasurehunt.revelocore.util;

import android.content.Context;

import com.sixsimplex.treasurehunt.revelocore.util.constants.DatabaseConstants;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;
import com.sixsimplex.treasurehunt.revelocore.util.sharedPreference.UserInfoPreferenceUtility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AppFolderStructure {

    private static String REVELO_FOLDER_NAME = "Revelo_3.0";
    private static String RE_GP_FOLDER_NAME = "Regp";
    private static String DATABASE_FOLDER_NAME = "Database";
    private static final String DATA_FOLDER = "data";
    private static final String ATTACHMENT_FOLDER = "Attachment";
    private static final String UPLOAD_FOLDER = "Upload";
    private static final String UPLOAD_ATTACHMENT_FOLDER = "attachments_upload";
    private static final String ATTACHMENT_DESCRIPTION_FILE = "description.json";
    private static final String UPLOAD_ATTACHMENT_ZIP_FOLDER = "attachments upload.zip";
    private static final String LOG_ZIP_FILE = "logZipToUpload.zip";

    public static File createReDbFolder(Context context) {
        if(getReGpFolderPath(context)!=null) {
            return createFile(getReGpFolderPath(context), true);
        }else return null;
    }

    private static String getReGpFolderPath(Context context) {
        if(getOrgFolderPath(context)!=null)
            return getOrgFolderPath(context) + File.separator + RE_GP_FOLDER_NAME;
        return null;
    }

    public static String getReGpFilePath(Context context) {
        if(getReGpFolderPath(context)!=null)
            return getReGpFolderPath(context) + File.separator + DatabaseConstants.RE_GP_NAME;
        return null;
    }

    private static String getReveloFolderPath(Context context) {
//        return Environment.getExternalStorageDirectory() + File.separator + REVELO_FOLDER_NAME;
        File revelofile = context.getExternalFilesDir(REVELO_FOLDER_NAME);
        if(!revelofile.exists()){
            revelofile.mkdirs();
        }
        return context.getExternalFilesDir(REVELO_FOLDER_NAME).getAbsolutePath();
    }

    public static void createOrgFolder(Context context){
        String orgPath = getOrgFolderPath(context);
        if(orgPath!=null) {
            createFile(orgPath, true);
        }
    }
    public static String getOrgFolderPath(Context context) {
        if(UserInfoPreferenceUtility.getOrgName().equalsIgnoreCase(""))
            return null;
        return getReveloFolderPath(context) + File.separator + UserInfoPreferenceUtility.getOrgName();
    }

    public static void createUserFolder(Context context){
        String userFolderPath = getUserFolderPath(context);
        if(userFolderPath!=null) {
            createFile(userFolderPath,true);
        }
    }

    private static String getUserFolderPath(Context context) {
        if(getOrgFolderPath(context)!=null && !UserInfoPreferenceUtility.getUserName().equalsIgnoreCase(""))
        return getOrgFolderPath(context) + File.separator + UserInfoPreferenceUtility.getUserName();
        else
            return null;
    }

    public static void createSurveyFolder(Context context){
        String surveyFolderPath = getSurveyFolderPath(context);
        if(surveyFolderPath!=null) {
            createFile(surveyFolderPath,true);
        }
    }

    private static String getSurveyFolderPath(Context context) {
        if(getUserFolderPath(context)!=null && !UserInfoPreferenceUtility.getSurveyName().isEmpty())
        return getUserFolderPath(context) + File.separator + UserInfoPreferenceUtility.getSurveyName();
        else return null;
    }
    private static String getPhaseFolderPath(Context context) {
        if(getUserFolderPath(context)!=null && !UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty())
        return getUserFolderPath(context) + File.separator + UserInfoPreferenceUtility.getSurveyName()+File.separator+UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName());
        else return null;
    }
    public static void createPhaseFolder(Context context) {
        String phaseFolderPath = getPhaseFolderPath(context);
        if(phaseFolderPath!=null) {
            createFile(phaseFolderPath,true);
        }
    }
    public static File createDataGpFolder(Context context) {
        if(getDataBaseFolderPath(context)!=null)
            return createFile(getDataBaseFolderPath(context), true);
        return null;
    }
    public static String getDataBaseFolderPath(Context context) {
        if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
            if (getPhaseFolderPath(context) != null)
                return getPhaseFolderPath(context) + File.separator + DATABASE_FOLDER_NAME;
            else return null;
        }else {
            if (getSurveyFolderPath(context) != null)
                return getSurveyFolderPath(context) + File.separator + DATABASE_FOLDER_NAME;
            else return null;
        }
    }

    private static String getDataGpPath(Context context) {
        if(UserInfoPreferenceUtility.getDataDbName().isEmpty())
            return null;
        return getDataBaseFolderPath(context) + File.separator + UserInfoPreferenceUtility.getDataDbName();
    }

    public static File createMetadataFolder(Context context) {
        if(getMetaDataFolderPath(context)!=null)
            return createFile(getMetaDataFolderPath(context), true);
        return null;
    }

    public static String getMetaDataFolderPath(Context context) {
        return getDataBaseFolderPath(context);
    }

    private static String getMetaGpPath(Context context) {
        if(UserInfoPreferenceUtility.getMetatdataDbName().isEmpty())//when app is installed again
            return null;
        return getMetaDataFolderPath(context) + File.separator + UserInfoPreferenceUtility.getMetatdataDbName();
    }

    private static String getDataFolderPath(Context context) {
        if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
            if (getPhaseFolderPath(context) != null)
                return getPhaseFolderPath(context) + File.separator + DATA_FOLDER;
            else return null;
        }else {
            if (getSurveyFolderPath(context) != null)
                return getSurveyFolderPath(context) + File.separator + DATA_FOLDER;
            else return null;
        }
    }

    private static String getAttachmentFolderPath(Context context) {
        return getDataFolderPath(context) + File.separator + ATTACHMENT_FOLDER;
    }

    private static String getAttachmentTypeFolderPath(Context context,String type) {
        return getAttachmentFolderPath(context) + File.separator + type;
    }

    public static File createAttachmentFile(Context context,String fileName, String type) {
        String attachmentFilePath = getAttachmentTypeFolderPath(context,type);
        File file = createFile(attachmentFilePath, true);
        return createFile(file.getAbsolutePath() + File.separator + fileName, false);
    }

    public static void deleteAttachmentFolder(Context context) {
        File attachmentFolder = new File(getDataFolderPath(context));
        deleteRecursive(attachmentFolder);
    }

    private static File createUploadFolder(Context context) {
        String dataFolderPath = getDataFolderPath(context);
        return createFile(dataFolderPath + File.separator + UPLOAD_FOLDER, true);
    }

    public static File createUploadAttachmentFolder(Context context) {
        String uploadFolderPath = createUploadFolder(context).getAbsolutePath();
        return createFile(uploadFolderPath + File.separator + UPLOAD_ATTACHMENT_FOLDER, true);
    }

    public static File createAttachmentDescriptionFile(Context context) {
        File uploadAttachmentFolder = createUploadAttachmentFolder(context);
        return createFile(uploadAttachmentFolder.getAbsolutePath() + File.separator + ATTACHMENT_DESCRIPTION_FILE, false);
    }

    public static File createUploadAttachmentZIP(Context context) {
        String uploadFolderPath = createUploadFolder(context).getAbsolutePath();
        return createFile(uploadFolderPath + File.separator + UPLOAD_ATTACHMENT_ZIP_FOLDER, false);
    }

    public static File createLogZipFile(Context context) {
        String logFolderPath = getUserFolderPath(context) + File.separator + LOG_ZIP_FILE;
        return createFile(logFolderPath, false);
    }

    /*------------------------------------------------------------------------------------------------------------------------------------*/

    public static File getReGp(Context context) {
        String regpFilePath = getReGpFilePath(context);
        boolean isfileExist = checkFileExistOrNot(regpFilePath);
        if (isfileExist) {
            return new File(regpFilePath);
        } else {
            return null;
        }
    }

    public static File getMetaGeoPackage(Context context) {
        ReveloLogger.debug("AppFolderStructure","getMetaGeoPackage","fetching meta geopackage file..");
        if (getMetaGpPath(context)!=null && !getMetaGpPath(context).isEmpty() && isMetaDataGpPresent(context)) {
            ReveloLogger.debug("AppFolderStructure","getMetaGeoPackage","isMetaDataGpPresent() - "+isMetaDataGpPresent(context));
            ReveloLogger.debug("AppFolderStructure","getMetaGeoPackage","returning file located at getMetaGpPath() - "+getMetaGpPath(context));
            return new File(getMetaGpPath(context));
        } else {
            ReveloLogger.debug("AppFolderStructure","getMetaGeoPackage","isMetaDataGpPresent() - "+isMetaDataGpPresent(context));
            ReveloLogger.debug("AppFolderStructure","getMetaGeoPackage","returning null");
            return null;
        }
    }

    public static File getDataGeoPackage(Context context) {
        if (getDataGpPath(context)!=null && !getDataGpPath(context).isEmpty() && isDataGpPresent(context)) {
            return new File(getDataGpPath(context));
        } else {
            return null;
        }
    }

    public static boolean isDataGpPresent(Context context) {
        String gdbFilePath = getDataGpPath(context);
        if(gdbFilePath==null ||gdbFilePath.isEmpty())
            return false;
        return checkFileExistOrNot(gdbFilePath);
    }

    public static boolean isMetaDataGpPresent(Context context) {
        String gdbFilePath = getMetaGpPath(context);
        if(gdbFilePath==null ||gdbFilePath.isEmpty())
            return false;
        return checkFileExistOrNot(gdbFilePath);
    }

    public static boolean isReGpPresent(Context context) {
        String gdbFilePath = getReGpFilePath(context);
        return checkFileExistOrNot(gdbFilePath);
    }

    public static File createLogFolder(Context context) {
        if(getLogFolderPath(context)==null||getLogFolderPath(context).isEmpty())
            return null;
        return createFile(getLogFolderPath(context), true);
    }

    public static String getLogFolderPath(Context context) {
        //return getReveloFolderPath(context) + File.separator + UserInfoPreferenceUtility.getUserName() + File.separator + "Log";
       if(UserInfoPreferenceUtility.getSurveyName().isEmpty() || UserInfoPreferenceUtility.getOrgName().isEmpty() || UserInfoPreferenceUtility.getUserName().isEmpty()){
           return null;
       }

        if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
            if (getPhaseFolderPath(context) != null)
                return getPhaseFolderPath(context) + File.separator + "Log";
            else return null;
        }else {
            if (getSurveyFolderPath(context) != null)
                return getSurveyFolderPath(context) + File.separator + "Log";
            else return null;
        }
    }
    public static String getMapTilesFolderPath(Context context) {
        //return getReveloFolderPath(context) + File.separator + UserInfoPreferenceUtility.getUserName() + File.separator + "Log";
       if(UserInfoPreferenceUtility.getSurveyName().isEmpty() || UserInfoPreferenceUtility.getOrgName().isEmpty() || UserInfoPreferenceUtility.getUserName().isEmpty()){
           return null;
       }

        if(!UserInfoPreferenceUtility.getSurveyPhaseName(UserInfoPreferenceUtility.getSurveyName()).isEmpty()){
            if (getPhaseFolderPath(context) != null)
                return getPhaseFolderPath(context) + File.separator + "tiles";
            else return null;
        }else {
            if (getSurveyFolderPath(context) != null)
                return getSurveyFolderPath(context) + File.separator + "tiles";
            else return null;
        }
    }

    public static File createTilesFolder(Context context) {
        if(getMapTilesFolderPath(context)==null||getMapTilesFolderPath(context).isEmpty())
            return null;
        return createFile(getMapTilesFolderPath(context),true);
    }

    public static File getBaseMapFile(Context context){
        if(getBaseMapCacheFolderPath(context)==null||getBaseMapCacheFolderPath(context).isEmpty())
            return null;
        return createFile(getBaseMapCacheFolderPath(context), false);
    }
    public static String getBaseMapCacheFolderPath(Context context) {
        //return getReveloFolderPath(context) + File.separator + UserInfoPreferenceUtility.getUserName() + File.separator + "Log";
        if(UserInfoPreferenceUtility.getSurveyName().isEmpty() || UserInfoPreferenceUtility.getOrgName().isEmpty() || UserInfoPreferenceUtility.getUserName().isEmpty()){
            return null;
        }
        return getSurveyFolderPath(context)  + File.separator + "BaseMapCache.gmef";
    }

   /* public static File createReDbFolder() {
        return createFile(getReDbFolderPath(), true);
    }

    public static File createDatabaseFolder() {
        return createFile(getDataBaseFolderPath(), true);
    }

    public static File createLogFolder() {
        return createFile(getLogFolderPath(), true);
    }

    public static File createLogFile(String fileName) {
        String logFilePath = getLogFolderPath() + File.separator + fileName;
        return createFile(logFilePath, false);
    }

    public static File createLogZipFile() {
        String logFolderPath = getUserFolderPath() + File.separator + AppConstants.LOG_ZIP_FILE;
        return createFile(logFolderPath, false);
    }

    public static File createGdbFile() {
        return createFile(getGdbFilePath(), false);
    }*/

    /*----------------------------------------------------------------------------------------------------------------------------------*/

   /* public static File createAttachmentFile(String fileName) {
        String attachmentFilePath = getAttachmentFolderPath() + File.separator + fileName;
        return createFile(attachmentFilePath, false);
    }

    public static File createAttachmentFolder() {
        return createFile(getAttachmentFolderPath(), true);
    }

    public static void deleteAttachmentFolder() {
        File attachmentFolder = createAttachmentFolder();
        deleteRecursive(attachmentFolder);
    }*/

   /* private static String getAttachmentFolderPath() {
        return getDataFolderPath() + File.separator + AppConstants.ATTACHMENT_FOLDER;
    }*/

    /*-----------------------------------------------------------------------------------------------------------------------------------*/

    /*-------------------------------------------------------------------------*/

    private static File createFile(String path, boolean isFolder) {

        File file = new File(path);
        try {
            if (!file.exists()) {
                if (isFolder) {
                    file.mkdirs();
                } else {
                    file.createNewFile();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }

   /* public static boolean isGdbPresent() {
        String gdbFilePath = getGdbFilePath();
        return checkFileExistOrNot(gdbFilePath);
    }

    public static boolean isMetaDbPresent() {
        String metaDbFilePath = getMetaDbFilePath();
        return checkFileExistOrNot(metaDbFilePath);
    }

    public static boolean isReDbPresent() {
        String reDbFilePath = getReDbFilePath();
        return checkFileExistOrNot(reDbFilePath);
    }*/

   public static void deleteRegpFolder(Context context) {
       deleteRecursive(createReDbFolder(context));
   }
    public static void deleteUploadFolder(Context context) {
        deleteRecursive(createUploadFolder(context));
    }
    public static void deleteLogFolder(Context context,boolean deleteAndRecreateOriginal) {
        deleteRecursive(createLogZipFile(context));
        if(deleteAndRecreateOriginal) {
            File logFolder = createLogFolder(context);
            if(logFolder!=null) {
                deleteRecursive(logFolder);
            }
            createLogFolder(context);
        }
    }
    private static void deleteRecursive(File fileOrDirectory) {
        try {
            if (fileOrDirectory.isDirectory()) {
                for (File child : fileOrDirectory.listFiles()) {
                    if (child.isDirectory()) {
                        deleteRecursive(child);
                    } else {
                        child.delete();
                    }
                }
            }
            fileOrDirectory.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkFileExistOrNot(String filePath) {
       if(filePath==null||filePath.isEmpty()){
           return false;
       }
        File reDbFile = new File(filePath);
        return reDbFile.exists();

    }

    public static void moveFile(File file, File dir) throws IOException {

        FileChannel outputChannel = null;
        FileChannel inputChannel = null;

        try {

            File newFile = new File(dir, file.getName());

            outputChannel = new FileOutputStream(newFile).getChannel();
            inputChannel = new FileInputStream(file).getChannel();
            inputChannel.transferTo(0, inputChannel.size(), outputChannel);

            file.delete();
        } finally {
            if (inputChannel != null) {
                inputChannel.close();
            }
            if (outputChannel != null) {
                outputChannel.close();
            }
        }
    }

    public static void createZipFile(File sourceFile, File file, boolean moveAllFilesToRoot) {
        final int BUFFER = 2048;

        try {

            if (file.exists()) {
                FileOutputStream dest = new FileOutputStream(file.getAbsolutePath());
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                if (sourceFile.isDirectory()) {
                    zipSubFolder(out, sourceFile, sourceFile.getParent().length(), moveAllFilesToRoot);
                } else {
                    byte[] data = new byte[BUFFER];
                    FileInputStream fi = new FileInputStream(sourceFile.getAbsolutePath());
                    BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry = new ZipEntry(getLastPathComponent(sourceFile.getAbsolutePath()));
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
    }

    private static String getLastPathComponent(String filePath) {
        String[] segments = filePath.split("/");
        return segments[segments.length - 1];
    }


    private static void zipSubFolder(ZipOutputStream out, File folder, int basePathLength, boolean moveAllFilesToRoot) throws IOException {

        final int BUFFER = 2048;

        File[] fileList = folder.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isDirectory()) {
                    zipSubFolder(out, file, basePathLength, moveAllFilesToRoot);
                } else {
                    byte[] data = new byte[BUFFER];
                    String unmodifiedFilePath = file.getPath();
                    String relativePath = unmodifiedFilePath.substring(basePathLength);
                    String rootPath = relativePath.substring(relativePath.lastIndexOf("/") + 1, relativePath.length());

                    FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                    BufferedInputStream origin = new BufferedInputStream(fi, BUFFER);
                    ZipEntry entry;
                    if (moveAllFilesToRoot) {
                        entry = new ZipEntry(rootPath);
                    } else {
                        entry = new ZipEntry(relativePath);
                    }
                    out.putNextEntry(entry);
                    int count;
                    while ((count = origin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                    origin.close();
                }
            }
        }
    }

    public static void resetAllVariables() {

    }

   /* public static void cleanAndCreateFolder(File file) {
        if (file.exists()) {
            //clean folder
            try {
                FileUtils.cleanDirectory(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
            //then create new empty folder
            try {
                if (!file.exists()) {
                    file.mkdirs();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            file.mkdirs();
        }
    }

    public static boolean isDataBasePresent() {

        boolean gdbPresent = isGdbPresent();
        boolean metaDbPresent = isMetaDbPresent();

        return gdbPresent && metaDbPresent;
    }*/
   public static String userProfilePictureFolderPath(Context context) {
       //return getUserFolderPath(context) + File.separator + UserInfoPreferenceUtility.getUserName();
       return getUserFolderPath(context) + File.separator + "profile";
   }

   public static String orgLogoFolderPath(Context context){
       return getOrgFolderPath(context)+File.separator+"orgLogo";
   }

}