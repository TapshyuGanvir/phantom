package com.sixsimplex.treasurehunt.Phantom1.appfragment.dashboard;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.ContentInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.sixsimplex.treasurehunt.Phantom1.app.IdeliveryActivityView;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.R;

public class DashBoardFragment extends Fragment {

    Context context;
    DashBoardFragmentPresenter dashBoardFragmentPresenter;
    LinearLayout board1;



    public DashBoardFragment(Context context,
                             DashBoardFragmentPresenter dashBoardFragmentPresenter) {
        this.context = context;
        this.dashBoardFragmentPresenter = dashBoardFragmentPresenter;

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dash_board, container, false);
        Board.setBoard(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public void updateDashBoard(){
        Board.updateDataSetChange();
    }

}