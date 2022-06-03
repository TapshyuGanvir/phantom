package com.sixsimplex.treasurehunt.revelocore.principalEndpoint.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Principal implements Parcelable {

    private String status;
    private String orgName;
    private String userName;
    private String firstName;
    private String lastName;
    private String position;
    private String role;

    private String jurisdictionName;
    private String jurisdictionType;

    private List<String> assignedProjects;
    private Privileges privilegesModel;

    private boolean isREDBDownloadRequired;
    private String serverREDBTimestamp;

    public Principal() {

    }

    public Principal(Parcel in) {
        status = in.readString();
        orgName = in.readString();
        userName = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        position = in.readString();
        role = in.readString();
        jurisdictionName = in.readString();
        jurisdictionType = in.readString();
        assignedProjects = in.createStringArrayList();
        isREDBDownloadRequired = in.readByte() != 0;
        serverREDBTimestamp = in.readString();
    }

    public static final Creator<Principal> CREATOR = new Creator<Principal>() {
        @Override
        public Principal createFromParcel(Parcel in) {
            return new Principal(in);
        }

        @Override
        public Principal[] newArray(int size) {
            return new Principal[size];
        }
    };

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getJurisdictionName() {
        return jurisdictionName;
    }

    public void setJurisdictionName(String jurisdictionName) {
        this.jurisdictionName = jurisdictionName;
    }

    public String getJurisdictionType() {
        return jurisdictionType;
    }

    public void setJurisdictionType(String jurisdictionType) {
        this.jurisdictionType = jurisdictionType;
    }

    public List<String> getAssignedProjects() {
        return assignedProjects;
    }

    public void setAssignedProjects(List<String> assignedProjects) {
        this.assignedProjects = assignedProjects;
    }

    public Privileges getPrivilegesModel() {
        return privilegesModel;
    }

    public void setPrivilegesModel(Privileges privilegesModel) {
        this.privilegesModel = privilegesModel;
    }

    public boolean isREDBDownloadRequired() {
        return isREDBDownloadRequired;
    }

    public void setREDBDownloadRequired(boolean REDBDownloadRequired) {
        isREDBDownloadRequired = REDBDownloadRequired;
    }

    public String getServerREDBTimestamp() {
        return serverREDBTimestamp;
    }

    public void setServerREDBTimestamp(String serverREDBTimestamp) {
        this.serverREDBTimestamp = serverREDBTimestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(orgName);
        dest.writeString(userName);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(position);
        dest.writeString(role);
        dest.writeString(jurisdictionName);
        dest.writeString(jurisdictionType);
        dest.writeStringList(assignedProjects);
        dest.writeByte((byte) (isREDBDownloadRequired ? 1 : 0));
        dest.writeString(serverREDBTimestamp);
    }
}
