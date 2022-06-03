package com.sixsimplex.treasurehunt.Phantom1.products;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;

import java.util.List;

public class ProductSpinnerAdapter extends ArrayAdapter<Feature> {

    Context context;
    List<Feature> valueList;
    LayoutInflater layoutInflater;
    String title="";

    public ProductSpinnerAdapter(@NonNull Context context, @NonNull List<Feature> valueList) {
        super(context, 0, valueList);
        this.context = context;
        this.valueList = valueList;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        @SuppressLint({"ViewHolder", "InflateParams"}) View rowView = layoutInflater.inflate(R.layout.spinneritem, null, true);
        Feature feature = getItem(position);
        TextView item_name = rowView.findViewById(R.id.product_spinner_item);
        String val=feature.getAttributes().get("productname") +" "+ (Double)feature.getAttributes().get("contcapacity")+"/"+feature.getAttributes().get("capunit");
        item_name.setText(val);
        return rowView;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View drop_dow_view = layoutInflater.inflate(R.layout.spinner_drop_down, parent, false);
        Feature feature = getItem(position);
        String val=feature.getAttributes().get("productname") +" "+ (Double)feature.getAttributes().get("contcapacity")+"/"+feature.getAttributes().get("capunit");
        TextView item_name = drop_dow_view.findViewById(R.id.item_name);
        item_name.setText(val);
        return drop_dow_view;
    }
    @Override
    public int getPosition(@Nullable Feature item) {
        return super.getPosition(item);
    }

    public void setTitle(String key_tv) {
        this.title=key_tv;
    }
}
