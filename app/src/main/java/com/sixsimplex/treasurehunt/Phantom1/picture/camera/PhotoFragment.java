package com.sixsimplex.treasurehunt.Phantom1.picture.camera;

import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;


import java.io.File;
import java.util.Objects;

public class PhotoFragment extends DialogFragment {

    private Uri uri;
    private FragmentActivity fragmentActivity;

    public static PhotoFragment newInstance(Uri uri, FragmentActivity fragmentActivity) {
        PhotoFragment photoFragment = new PhotoFragment();
        photoFragment.setUri(uri);
        photoFragment.setCameraInstance(fragmentActivity);
        return photoFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.dialog_theme);
    }

    private void setUri(Uri uri) {
        this.uri = uri;
    }

    private void setCameraInstance(FragmentActivity fragmentActivity) {
        this.fragmentActivity = fragmentActivity;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragmen_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backButton = view.findViewById(R.id.back_button);
        ImageView photo_preview = view.findViewById(R.id.photo_preview);
        ImageView saveIv = view.findViewById(R.id.saveIv);
        EditText captionEt = view.findViewById(R.id.captionEt);

        Glide.with(this).load(uri).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(photo_preview);

        backButton.setOnClickListener(v -> {
            deleteCaptureFile();
            dismiss();
        });

        saveIv.setOnClickListener(v -> {
            String caption = captionEt.getText().toString();
            File fileCapture = new File(Objects.requireNonNull(uri.getPath()));

            Intent output = new Intent();
            output.putExtra(AppConstants.caption, caption);
            getActivity().setResult(RESULT_OK, output);
            getActivity().finish();

            dismiss();
        });
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                // On backpress, do your stuff here.
                deleteCaptureFile();
                dismiss();
                fragmentActivity.finish();
            }
        };
    }

    private void deleteCaptureFile() {
        if (uri != null) {
            File fdelete = new File(Objects.requireNonNull(uri.getPath()));
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    System.out.println("file Deleted :" + uri.getPath());
                } else {
                    System.out.println("file not Deleted :" + uri.getPath());
                }
            }
        }
    }
}