package com.sixsimplex.treasurehunt.revelocore.editing.model;

import java.io.File;

public class Attachment {

    private String w9Id, contentType, formType, dateTimeStamp, label, userName, userRole, attachmentName;
    private File file;
    private boolean isDeleteAttachmentFromDb;
    private int isNew, size;
    private double lat, lng, zValue, accuracy;

    public String getW9Id() {
        return w9Id;
    }

    public void setW9Id(String w9Id) {
        this.w9Id = w9Id;
    }

    public int getIsNew() {
        return isNew;
    }

    public void setIsNew(int isNew) {
        this.isNew = isNew;
    }

    public boolean isDeleteAttachmentFromDb() {
        return isDeleteAttachmentFromDb;
    }

    public void setDeleteAttachmentFromDb(boolean deleteAttachmentFromDb) {
        isDeleteAttachmentFromDb = deleteAttachmentFromDb;
    }

    public String getContentType() {
        return contentType;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(String dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getZValue() {
        return zValue;
    }

    public void setZValue(double zValue) {
        this.zValue = zValue;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getPath() {
        if (getFile() != null) {
            return getFile().getAbsolutePath();
        } else {
            return "";
        }
    }

}