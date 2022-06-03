package com.sixsimplex.treasurehunt.revelocore.util;

public class Operation {

    public static final String MEASUREMENT = "measurement";
    public static final String FEATURE_SELECTION = "featureSelection";
    public static final String SEARCH_RESULT = "searchResult";
    public static final String FEATURE_DRAWING = "featureDrawing";
    public static final String FEATURE_DRAWING_EDIT_GEOMETRY = "editGeometry";
    public static final String FEATURE_DRAWING_EDIT_PROPERTIES = "editAttributes";
    public static final String ADD_BOTTOM_SHEET = "closeAddBottomSheet";

    private static String operationType = "";
    private static String operationSubType = "";

    public static String getOperationType() {
        return operationType;
    }

    public static void setOperationType(String operation) {
        operationType = operation;
    }

    public static String getOperationSubType() {
        return operationSubType;
    }

    public static void setOperationSubType(String operationSubType) {
        Operation.operationSubType = operationSubType;
    }
}
