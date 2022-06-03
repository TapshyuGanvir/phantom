package com.sixsimplex.treasurehunt.revelocore.initialsetup;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.geopackage.models.Jurisdiction;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import java.util.List;

public class UserJurisdictionsAdapter extends RecyclerView.Adapter<UserJurisdictionsAdapter.ViewHolder> {

    private List<Jurisdiction> userJusrisdictionModelList;
    private String className = "UserJurisdictionsAdapter";

    public UserJurisdictionsAdapter(List<Jurisdiction> userJusrisdictionModelList) {
        this.userJusrisdictionModelList = userJusrisdictionModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_jurisdiciton_adapter, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        Jurisdiction userJusrisdictionModel = userJusrisdictionModelList.get(position);

        viewHolder.jurisdictionName.setText(userJusrisdictionModel.getName());
        viewHolder.jurisdictionValue.setText(userJusrisdictionModel.getType());

        ReveloLogger.debug(className, "onBindViewHolder", "Initialize views");
    }

    @Override
    public int getItemCount() {
        return userJusrisdictionModelList.size();
    }

     static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView jurisdictionName, jurisdictionValue;

        ViewHolder(View view) {
            super(view);
            jurisdictionName = view.findViewById(R.id.jurisdictionNameTV);
            jurisdictionValue = view.findViewById(R.id.jurisdictionValueTV);

        }
    }
}
