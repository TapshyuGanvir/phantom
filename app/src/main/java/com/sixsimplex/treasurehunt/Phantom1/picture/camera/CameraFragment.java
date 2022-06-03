package com.sixsimplex.treasurehunt.Phantom1.picture.camera;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.common.util.concurrent.ListenableFuture;
import com.sixsimplex.treasurehunt.Phantom1.picture.PictureActivity;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.AppFolderStructure;
import com.sixsimplex.treasurehunt.revelocore.util.constants.AppConstants;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import kotlin.jvm.internal.Intrinsics;

public class CameraFragment extends Fragment {

    private static String mNextImageAbsolutePath;
    private static String imageName;
    private ConstraintLayout container = null;
    private PreviewView viewFinder = null;
    private LocalBroadcastManager broadcastManager;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private Preview preview = null;
    private ImageCapture imageCapture = null;
    private ExecutorService cameraExecutor;
    private String TAG = "CameraXBasic";

    private double RATIO_4_3_VALUE = 4.0 / 3.0;
    private double RATIO_16_9_VALUE = 16.0 / 9.0;
    private static String KEY_EVENT_ACTION = "key_event_action";
    private static String KEY_EVENT_EXTRA = "key_event_extra";
    private static GetAttachmentInfoInterface getAttachmentInfoInterface;


    private BroadcastReceiver volumeDownReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int event = intent.getIntExtra(KEY_EVENT_EXTRA, KeyEvent.KEYCODE_UNKNOWN);

            if (event == KeyEvent.KEYCODE_VOLUME_DOWN) {
                captureImage();
                final Handler handler = new Handler();
                handler.postDelayed(() -> {
                    //add your code here
                }, 100);
            }
        }
    };

    /*@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.dialog_theme);
    }*/

    public static CameraFragment newInstance(String imagePath, String name, GetAttachmentInfoInterface getAttachmentInfoInterface) {
//        mNextImageAbsolutePath = imagePath;
        imageName = name;
        CameraFragment.getAttachmentInfoInterface=getAttachmentInfoInterface;
        return new CameraFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cameraExecutor = Executors.newSingleThreadExecutor();
        broadcastManager = LocalBroadcastManager.getInstance(view.getContext());
        container = view.findViewById(R.id.camera_container);
        viewFinder = container.findViewById(R.id.view_finder);

        IntentFilter filter = new IntentFilter();
        filter.addAction(KEY_EVENT_ACTION);
        broadcastManager.registerReceiver(volumeDownReceiver, filter);

        viewFinder.post(() -> {
            updateCameraUi(); // Build UI controls
            bindCameraUseCases(); // Bind use cases
        });
    }

    private void updateCameraUi() {

        ConstraintLayout constraintLayoutContainer = this.container;
        if (constraintLayoutContainer == null) {
            Intrinsics.throwUninitializedPropertyAccessException("container");
        }

        constraintLayoutContainer = constraintLayoutContainer.findViewById(R.id.camera_ui_container);
        if (constraintLayoutContainer != null) {
            if (constraintLayoutContainer == null) {
                Intrinsics.throwUninitializedPropertyAccessException("container");
            }
            constraintLayoutContainer.removeView((View) constraintLayoutContainer);
        }

        Context var7 = this.requireContext();
        ConstraintLayout constraintLayout = this.container;
        if (constraintLayout == null) {
            Intrinsics.throwUninitializedPropertyAccessException("container");
        }

        View controls = View.inflate(var7, R.layout.camera_ui_container, constraintLayout);

        controls.findViewById(R.id.camera_capture_button).setOnClickListener((it -> {
            captureImage();
        }));

        controls.findViewById(R.id.camera_switch_button).setOnClickListener((it -> {
            lensFacing = lensFacing == 0 ? 1 : 0;
            bindCameraUseCases();
        }));
    }

    private void captureImage() {

        if (imageCapture != null) {
            String imageName = System.currentTimeMillis() + AppConstants.imageExtension;
            File imageFile = AppFolderStructure.createAttachmentFile(requireActivity(),imageName, AppConstants.photo);
//            File photoFile = new File(mNextImageAbsolutePath);
            ImageCapture.Metadata metadata = new ImageCapture.Metadata();
            metadata.setReversedHorizontal(lensFacing == 0);
            ImageCapture.OutputFileOptions outputOptions = (new ImageCapture.OutputFileOptions.Builder(imageFile)).setMetadata(metadata).build();
            Intrinsics.checkExpressionValueIsNotNull(outputOptions, "ImageCapture.OutputFile.build()");

            imageCapture.takePicture(outputOptions, cameraExecutor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {

                    Uri uri = outputFileResults.getSavedUri();
                    if (uri == null) {
                        uri = Uri.fromFile(imageFile);
                    }

                    Uri finalUri = uri;

                    requireActivity().runOnUiThread(() -> {
                        File fileCapture = new File(Objects.requireNonNull(finalUri.getPath()));
                        getAttachmentInfoInterface.getCaptureImageInfo(fileCapture, AppConstants.imageType);
//                        PhotoFragment photoFragment = PhotoFragment.newInstance(finalUri, getActivity());
//                        assert getFragmentManager() != null;
//                        photoFragment.show(getFragmentManager(), "Photo View");
//
//                        Log.d(TAG, "Photo capture succeeded: " + finalUri);
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    Log.d(TAG, "Photo capture Failed: " + exception.getMessage());
                    requireActivity().runOnUiThread(() -> Toast.makeText(getActivity(), exception.getMessage(), Toast.LENGTH_SHORT).show());
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateCameraUi();
    }

    private int aspectRatio(int width, int height) {
        double var8 = (double) Math.max(width, height);
        int var10 = Math.min(width, height);
        double previewRatio = var8 / (double) var10;
        double var13 = previewRatio - RATIO_4_3_VALUE;
        double var10000 = Math.abs(var13);
        var13 = previewRatio - RATIO_16_9_VALUE;
        var8 = var10000;
        double var12 = Math.abs(var13);
        return var8 <= var12 ? 0 : 1;
    }

    @SuppressLint("RestrictedApi")
    private void bindCameraUseCases() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        PreviewView previewView = this.viewFinder;
        if (previewView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewFinder");
        }

        previewView.getDisplay().getRealMetrics(displayMetrics);
        Log.d("CameraXBasic", "Screen metrics: " + displayMetrics.widthPixels + " x " + displayMetrics.heightPixels);
        int screenAspectRatio = this.aspectRatio(displayMetrics.widthPixels, displayMetrics.heightPixels);
        Log.d("CameraXBasic", "Preview aspect ratio: " + screenAspectRatio);

        previewView = this.viewFinder;
        if (previewView == null) {
            Intrinsics.throwUninitializedPropertyAccessException("viewFinder");
        }

        Display display = previewView.getDisplay();
        Intrinsics.checkExpressionValueIsNotNull(display, "viewFinder.display");
        final int rotation = display.getRotation();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        Intrinsics.checkExpressionValueIsNotNull(cameraSelector, "CameraSelector.Builder()…acing(lensFacing).build()");
        ListenableFuture cameraProviderFuture = ProcessCameraProvider.getInstance(this.requireContext());
        Intrinsics.checkExpressionValueIsNotNull(cameraProviderFuture, "ProcessCameraProvider.ge…nstance(requireContext())");

        cameraProviderFuture.addListener((() -> {
            try {
                Object object = cameraProviderFuture.get();
                Intrinsics.checkExpressionValueIsNotNull(object, "cameraProviderFuture.get()");
                ProcessCameraProvider cameraProvider = (ProcessCameraProvider) object;

                preview = new Preview.Builder()
                        // We request aspect ratio but no resolution
                        .setTargetAspectRatio(screenAspectRatio)
                        // Set initial target rotation
//                        .setMaxResolution(new Size(640, 740))
//                        .setDefaultResolution(new Size(640, 740))
                        .setTargetRotation(rotation)
                        .build();

                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        // We request aspect ratio but no resolution to match preview config, but letting
                        // CameraX optimize for whatever specific resolution best fits our use cases
                        .setTargetAspectRatio(screenAspectRatio)
                        .setMaxResolution(new Size(640, 740))
                        .setDefaultResolution(new Size(640, 740))
                        // Set initial target rotation, we will have to call this again if rotation changes
                        // during the lifecycle of this use case
                        .setTargetRotation(rotation)
                        .build();
                cameraProvider.unbindAll();

                // A variable number of use-cases can be passed here -
                // camera provides access to CameraControl & CameraInfo
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }), ContextCompat.getMainExecutor(this.requireContext()));
    }
}