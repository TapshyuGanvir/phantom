package com.sixsimplex.treasurehunt.revelocore.editing.model;

import android.view.View;

public class AttributeTagModel {

    private String value;
    private View view;
    private String name;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
