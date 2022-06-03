package com.sixsimplex.treasurehunt.revelocore.util.bottom_sheet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.sixsimplex.treasurehunt.R;

import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class InfoBottomSheet extends BottomSheetDialogFragment {

    @BindViews({R.id.yesLogOutBtn, R.id.noLogOutBtn})
    List<Button> buttonList;

    @BindViews({R.id.titleBottomTv, R.id.messageTv})
    List<TextView> textViewList;

    private Activity activity;
    private String ok, cancel, message, titleMessage;
    private int requestCode,errorCode;
    private InfoBottomSheetInterface infoBottomSheetInterface;
    private String jurisdiction;

    public static InfoBottomSheet geInstance(Activity activity,
                                             String ok,
                                             String cancel,
                                             String title,
                                             String message,
                                             int requestCode,int errorCode, String data) {

        InfoBottomSheet infoBottomSheet = new InfoBottomSheet();
        Bundle bundle = new Bundle();
        infoBottomSheet.setParentActivity(activity);
        infoBottomSheet.setOkBtn(ok);
        infoBottomSheet.setCancelBtn(cancel);
        infoBottomSheet.setRequestCode(requestCode);
        infoBottomSheet.setTitle(title);
        infoBottomSheet.setMessage(message);
        infoBottomSheet.setJurisdictions(data);
        infoBottomSheet.setArguments(bundle);
        infoBottomSheet.setErrorCode(errorCode);
        //infoBottomSheet.setCancelable(false);
        return infoBottomSheet;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

    private void setJurisdictions(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    private void setParentActivity(Activity parentActivity) {
        this.activity = parentActivity;
    }

    private void setOkBtn(String ok) {
        this.ok = ok;
    }

    private void setCancelBtn(String cancel) {
        this.cancel = cancel;
    }

    private void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    private void setTitle(String title) {
        titleMessage = title;
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_info, null);
        dialog.setContentView(view);

        ButterKnife.bind(this, view);

        infoBottomSheetInterface = (InfoBottomSheetInterface) getActivity();
        dialog.setCancelable(false);

        if (!TextUtils.isEmpty(titleMessage)) {
            textViewList.get(0).setVisibility(View.VISIBLE);
            textViewList.get(0).setText(titleMessage);
        } else {
            textViewList.get(0).setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(message)) {
            textViewList.get(1).setText(message);
        }

        if (!TextUtils.isEmpty(ok)) {
            buttonList.get(0).setVisibility(View.VISIBLE);
            buttonList.get(0).setText(ok);
        } else {
            buttonList.get(0).setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(cancel)) {
            buttonList.get(1).setVisibility(View.VISIBLE);
            buttonList.get(1).setText(cancel);
        } else {
            buttonList.get(1).setVisibility(View.GONE);
        }
    }

    @OnClick({R.id.yesLogOutBtn, R.id.noLogOutBtn})
    public void OnClickEvent(View view) {

        switch (view.getId()) {

            case R.id.yesLogOutBtn:
                dismiss();
                infoBottomSheetInterface.onOkInfoBottomSheetResult(requestCode,errorCode, jurisdiction);

                break;

            case R.id.noLogOutBtn:
                dismiss();
                infoBottomSheetInterface.onCancelOkBottomSheetResult(requestCode,errorCode);
                break;
        }
    }
}
