package com.sixsimplex.treasurehunt.revelocore.userProfile;
import java.io.Serializable;

public class UserProfileModel implements Serializable {

    private String userName;
    private String creationDate;
    private String startDate;
    private String endDate;
    private boolean viewerEnable;
    private boolean editorEnable;
    private boolean geoAdd;
    private boolean geoUpdate;
    private boolean geoDelete;
    private boolean attributeEnable;
    private boolean dashboardEnable;
    private boolean userEnable;
    private boolean downloadEnable;
    private boolean downloadAttachImage;
    private boolean downloadAttachVideo;
    private boolean downloadEntitiesShapefile;
    private boolean downloadEntitiesExcel;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isViewerEnable() {
        return viewerEnable;
    }

    public void setViewerEnable(boolean viewerEnable) {
        this.viewerEnable = viewerEnable;
    }

    public boolean isEditorEnable() {
        return editorEnable;
    }

    public void setEditorEnable(boolean editorEnable) {
        this.editorEnable = editorEnable;
    }

    public boolean isGeoAdd() {
        return geoAdd;
    }

    public void setGeoAdd(boolean geoAdd) {
        this.geoAdd = geoAdd;
    }

    public boolean isGeoUpdate() {
        return geoUpdate;
    }

    public void setGeoUpdate(boolean geoUpdate) {
        this.geoUpdate = geoUpdate;
    }

    public boolean isGeoDelete() {
        return geoDelete;
    }

    public void setGeoDelete(boolean geoDelete) {
        this.geoDelete = geoDelete;
    }

    public boolean isAttributeEnable() {
        return attributeEnable;
    }

    public void setAttributeEnable(boolean attributeEnable) {
        this.attributeEnable = attributeEnable;
    }

    public boolean isDashboardEnable() {
        return dashboardEnable;
    }

    public void setDashboardEnable(boolean dashboardEnable) {
        this.dashboardEnable = dashboardEnable;
    }

    public boolean isUserEnable() {
        return userEnable;
    }

    public void setUserEnable(boolean userEnable) {
        this.userEnable = userEnable;
    }

    public boolean isDownloadEnable() {
        return downloadEnable;
    }

    public void setDownloadEnable(boolean downloadEnable) {
        this.downloadEnable = downloadEnable;
    }

    public boolean isDownloadAttachImage() {
        return downloadAttachImage;
    }

    public void setDownloadAttachImage(boolean downloadAttachImage) {
        this.downloadAttachImage = downloadAttachImage;
    }

    public boolean isDownloadAttachVideo() {
        return downloadAttachVideo;
    }

    public void setDownloadAttachVideo(boolean downloadAttachVideo) {
        this.downloadAttachVideo = downloadAttachVideo;
    }

    public boolean isDownloadEntitiesShapefile() {
        return downloadEntitiesShapefile;
    }

    public void setDownloadEntitiesShapefile(boolean downloadEntitiesShapefile) {
        this.downloadEntitiesShapefile = downloadEntitiesShapefile;
    }

    public boolean isDownloadEntitiesExcel() {
        return downloadEntitiesExcel;
    }

    public void setDownloadEntitiesExcel(boolean downloadEntitiesExcel) {
        this.downloadEntitiesExcel = downloadEntitiesExcel;
    }
}