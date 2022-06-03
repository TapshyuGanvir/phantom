package com.sixsimplex.treasurehunt.revelocore.principalEndpoint.model;

public class Privileges {

    private boolean isViewerEnabled;
    private boolean isEditorEnabled;
    private boolean isAddGeometry;
    private boolean isUpdateGeometry;
    private boolean isDeleteGeometry;
    private boolean isAttributeEnable;
    private boolean isDashboardEnabled;
    private boolean isUserEnabled;
    private boolean isDownloadsEnabled;
    private boolean isImageDownload;
    private boolean isVideoDownload;
    private boolean isShapeFileDownload;
    private boolean isExcelDownload;
    private boolean isAllDownload;

    public boolean isViewerEnabled() {
        return isViewerEnabled;
    }

    public void setViewerEnabled(boolean viewerEnabled) {
        isViewerEnabled = viewerEnabled;
    }

    public boolean isEditorEnabled() {
        return isEditorEnabled;
    }

    public void setEditorEnabled(boolean editorEnabled) {
        isEditorEnabled = editorEnabled;
    }

    public boolean isAddGeometry() {
        return isAddGeometry;
    }

    public void setAddGeometry(boolean addGeometry) {
        isAddGeometry = addGeometry;
    }

    public boolean isUpdateGeometry() {
        return isUpdateGeometry;
    }

    public void setUpdateGeometry(boolean updateGeometry) {
        isUpdateGeometry = updateGeometry;
    }

    public boolean isDeleteGeometry() {
        return isDeleteGeometry;
    }

    public void setDeleteGeometry(boolean deleteGeometry) {
        isDeleteGeometry = deleteGeometry;
    }

    public boolean isAttributeEnable() {
        return isAttributeEnable;
    }

    public void setAttributeEnable(boolean attributeEnable) {
        isAttributeEnable = attributeEnable;
    }

    public boolean isDashboardEnabled() {
        return isDashboardEnabled;
    }

    public void setDashboardEnabled(boolean dashboardEnabled) {
        isDashboardEnabled = dashboardEnabled;
    }

    public boolean isUserEnabled() {
        return isUserEnabled;
    }

    public void setUserEnabled(boolean userEnabled) {
        isUserEnabled = userEnabled;
    }

    public boolean isDownloadsEnabled() {
        return isDownloadsEnabled;
    }

    public void setDownloadsEnabled(boolean downloadsEnabled) {
        isDownloadsEnabled = downloadsEnabled;
    }

    public boolean isImageDownload() {
        return isImageDownload;
    }

    public void setImageDownload(boolean imageDownload) {
        isImageDownload = imageDownload;
    }

    public boolean isVideoDownload() {
        return isVideoDownload;
    }

    public void setVideoDownload(boolean videoDownload) {
        isVideoDownload = videoDownload;
    }

    public boolean isShapeFileDownload() {
        return isShapeFileDownload;
    }

    public void setShapeFileDownload(boolean shapeFileDownload) {
        isShapeFileDownload = shapeFileDownload;
    }

    public boolean isExcelDownload() {
        return isExcelDownload;
    }

    public void setExcelDownload(boolean excelDownload) {
        isExcelDownload = excelDownload;
    }

    public boolean isAllDownload() {
        return isAllDownload;
    }

    public void setAllDownload(boolean allDownload) {
        isAllDownload = allDownload;
    }
}
