package com.sixsimplex.treasurehunt.revelocore.util;

import org.json.JSONObject;

public class ReveloOperationReturnType extends JSONObject {
    public static String RETURN_TYPE_OPERATION_STATUS_SUCCESS="success";
    public static String RETURN_TYPE_OPERATION_STATUS_FAILURE="failure";
    String operationStatus ;
    String operationMessage ;
    Object operationResult;

    private ReveloOperationReturnType(String operationStatus, String operationMessage, Object result){
        this.operationStatus=operationStatus;
        this.operationMessage=operationMessage;
        if(operationMessage==null ||operationMessage.isEmpty()||operationMessage.equalsIgnoreCase("null")){
            this.operationMessage = "Information Unavailable";
        }
        this.operationResult=result;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getOperationMessage() {
        return operationMessage;
    }

    public void setOperationMessage(String operationMessage) {
        this.operationMessage = operationMessage;
    }

    public Object getOperationResult() {
        return operationResult;
    }

    public void setOperationResult(Object operationResult) {
        this.operationResult = operationResult;
    }
}
