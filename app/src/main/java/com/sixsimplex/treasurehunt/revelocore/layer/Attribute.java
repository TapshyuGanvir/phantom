package com.sixsimplex.treasurehunt.revelocore.layer;

import android.text.TextUtils;
import android.view.View;

import com.sixsimplex.treasurehunt.revelocore.util.DatePickerMethods;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Attribute {

    transient private View view;

    private int entityPropertyId;

    private String name;
    private String type;
    private String label;
    private String entityName;
    private String defaultValue;
    private String groupLabel;

    private int groupNumber;
    private int index;

    private boolean isMandatory;
    private boolean isSystem;
    private boolean isExternal;
    private boolean enable;
    private boolean isOmniSearchEnabled;
    private boolean isDependant;

    private JSONObject domainObject;
    private String domainName;

    public static JSONArray attributeJsonArray(List<Attribute> attributeList){
        JSONArray attributeJArray = new JSONArray();
        try{
            for(Attribute attribute:attributeList){
               JSONObject attrJobj = attribute.toJson() ;
               attributeJArray.put(attrJobj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return attributeJArray;
    }

    public static List<Attribute> parseAttributeJsonArray(JSONArray attributeJsonArray) {

        List<Attribute> attributeList = new ArrayList<>();

        for (int i = 0; i < attributeJsonArray.length(); i++) {
            try {
                JSONObject attributeJsonObject = attributeJsonArray.getJSONObject(i);
                Attribute attributeViewModel = parseAttributeJson(attributeJsonObject);
                attributeList.add(attributeViewModel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return attributeList;
    }

    private static Attribute parseAttributeJson(JSONObject attributeJsonObject) {

        Attribute attributeViewModel = new Attribute();

        try {

            int propertyId = attributeJsonObject.has("entityPropertyId") ? attributeJsonObject.getInt("entityPropertyId") : -1;

            String propertyName = attributeJsonObject.has("name") ? attributeJsonObject.getString("name") : "";
            String propertyType = attributeJsonObject.has("type") ? attributeJsonObject.getString("type") : "";
            String propertyLabel = attributeJsonObject.has("label") ? attributeJsonObject.getString("label") : "";
            String propertyDefaultValue = attributeJsonObject.has("defaultValue") ? attributeJsonObject.getString("defaultValue") : "";
            String propertyEntityName = attributeJsonObject.has("entity") ? attributeJsonObject.getString("entity") : "";

            int propertyIndex = attributeJsonObject.has("index") ? attributeJsonObject.getInt("index") : -1;

            boolean propertyIsSystem = attributeJsonObject.has("isSystem") && attributeJsonObject.getBoolean("isSystem");
            boolean propertyIsExternal = attributeJsonObject.has("isExternal") && attributeJsonObject.getBoolean("isExternal");
            boolean propertyEnabled = attributeJsonObject.has("enabled") && attributeJsonObject.getBoolean("enabled");
            boolean propertyIsMandatory = attributeJsonObject.has("isMandatory") && attributeJsonObject.getBoolean("isMandatory");
            boolean propertyIsOmniSearchEnabled = attributeJsonObject.has("isOmniSearchEnabled") && attributeJsonObject.getBoolean("isOmniSearchEnabled");

            JSONObject propertyDomain = attributeJsonObject.has("domain") ? attributeJsonObject.getJSONObject("domain") : null;
            String propertyDomainName = attributeJsonObject.has("domainName") ? attributeJsonObject.getString("domainName") : null;

            attributeViewModel.setEntityPropertyId(propertyId);
            attributeViewModel.setName(propertyName);
            attributeViewModel.setType(propertyType);
            attributeViewModel.setLabel(propertyLabel);
            attributeViewModel.setDefaultValue(propertyDefaultValue);
            attributeViewModel.setEntityName(propertyEntityName);
            attributeViewModel.setIndex(propertyIndex);
            attributeViewModel.setSystem(propertyIsSystem);
            attributeViewModel.setExternal(propertyIsExternal);
            attributeViewModel.setEnable(propertyEnabled);
            attributeViewModel.setMandatory(propertyIsMandatory);
            attributeViewModel.setOmniSearchEnabled(propertyIsOmniSearchEnabled);
            attributeViewModel.setDomainObject(propertyDomain);
            attributeViewModel.setDomainName(propertyDomainName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return attributeViewModel;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getDomainName() {
        return domainName;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public int getEntityPropertyId() {
        return entityPropertyId;
    }

    public void setEntityPropertyId(int entityPropertyId) {
        this.entityPropertyId = entityPropertyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Object getDefaultValue() {
        Object value =null;
            try{
            switch (type) {
                case "string":
                case "text":
                    if (defaultValue==null || TextUtils.isEmpty(defaultValue)) {
                        value = "na";
                    } else {
                        value = defaultValue;
                    }
                    break;
                case "integer":
                case "int":
                    if (defaultValue==null || TextUtils.isEmpty(defaultValue)) {
                        value = 0;
                    } else {
                        try {
                            value = Integer.valueOf(defaultValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                            value = 0;
                        }
                    }
                    break;
                case "float":
                    if (defaultValue==null||TextUtils.isEmpty(defaultValue)) {
                        value = 0.0f;
                    } else {
                        try {
                            value = Double.valueOf(defaultValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                            value = 0.0f;
                        }
                    }
                    break;
                case "double":

                    if (defaultValue==null||TextUtils.isEmpty(defaultValue)) {
                        value = 0.0d;
                    } else {
                        try {
                            value = Double.valueOf(defaultValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                            value = 0.0d;
                        }
                    }
                    break;
                case "boolean":
                    if (defaultValue==null||TextUtils.isEmpty(defaultValue)) {
                        value = false;
                    } else {
                        try {
                            value = Boolean.valueOf(defaultValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                            value = false;
                        }
                    }
                    break;
                case "date":
                    if (TextUtils.isEmpty(defaultValue)) {
                        value = DatePickerMethods.getDefaultDateString();
                    } else {
                        try {
                            value = defaultValue;
                        } catch (Exception e) {
                            e.printStackTrace();
                            value = DatePickerMethods.getDefaultDateString();
                        }
                    }
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            return defaultValue;
        }
        return value;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public boolean isOmniSearchEnabled() {
        return isOmniSearchEnabled;
    }

    public void setOmniSearchEnabled(boolean omniSearchEnabled) {
        isOmniSearchEnabled = omniSearchEnabled;
    }

    public JSONObject getDomainObject() {
        return domainObject;
    }

    public void setDomainObject(JSONObject domainObject) {
        this.domainObject = domainObject;
    }

    public boolean isDependant() {
        return isDependant;
    }

    public void setDependant(boolean dependant) {
        isDependant = dependant;
    }

    public String getGroupLabel() {
        return groupLabel;
    }

    public void setGroupLabel(String groupLabel) {
        this.groupLabel = groupLabel;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    public void setGroupNumber(int groupNumber) {
        this.groupNumber = groupNumber;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "view=" + view +
                ", entityPropertyId=" + entityPropertyId +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", label='" + label + '\'' +
                ", entityName='" + entityName + '\'' +
                ", defaultValue='" + defaultValue + '\'' +
                ", groupLabel='" + groupLabel + '\'' +
                ", groupNumber=" + groupNumber +
                ", index=" + index +
                ", isMandatory=" + isMandatory +
                ", isSystem=" + isSystem +
                ", isExternal=" + isExternal +
                ", enable=" + enable +
                ", isOmniSearchEnabled=" + isOmniSearchEnabled +
                ", isDependant=" + isDependant +
                ", domainObject=" + domainObject +
                ", domainName=" + domainName +
                '}';
    }

   public JSONObject toJson(){
        JSONObject attrJson = new JSONObject();
       try {
            attrJson.put("view",view);
            attrJson.put("entityPropertyId",entityPropertyId);
            attrJson.put("name",name);
            attrJson.put("type",type);
            attrJson.put("label",label);
            attrJson.put("entityName",entityName);
            attrJson.put("defaultValue",defaultValue);
            attrJson.put("groupLabel",groupLabel);
            attrJson.put("groupNumber",groupNumber);
            attrJson.put("index",index);
            attrJson.put("isMandatory",isMandatory);
            attrJson.put("isSystem",isSystem);
            attrJson.put("isExternal",isExternal);
            attrJson.put("enable",enable);
            attrJson.put("isOmniSearchEnabled",isOmniSearchEnabled);
            attrJson.put("isDependant",isDependant);
            attrJson.put("domainObject",domainObject);
            attrJson.put("domainName",domainName);
       } catch (Exception e) {
           e.printStackTrace();
       }
        return attrJson;
   }
}
