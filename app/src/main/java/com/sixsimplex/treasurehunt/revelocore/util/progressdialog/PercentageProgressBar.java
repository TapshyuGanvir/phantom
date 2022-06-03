package com.sixsimplex.treasurehunt.revelocore.util.progressdialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import java.text.DecimalFormat;

public class PercentageProgressBar {

    private Dialog mDialog;
    private TextView titleTv;
    private ProgressBar horizontalBar, circularBar;
    private LinearLayout speedSizeLl;
    private TextView speedTv;
    private TextView downloadSizeTv;

    public static final int HORIZONTAL = 0;
    public static final int CIRCULAR = 1;
    private String className = "PercentageProgressBar";

    public PercentageProgressBar(Context context) {

        mDialog = new Dialog(context, R.style.AppTheme);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);

        mDialog.setContentView(R.layout.percentage_progress_dialog_view);

        horizontalBar = mDialog.findViewById(R.id.horizontalBar);
        circularBar = mDialog.findViewById(R.id.circularBar);

        titleTv = mDialog.findViewById(R.id.titleTv);
        speedSizeLl = mDialog.findViewById(R.id.speedSizeLl);
        speedTv = mDialog.findViewById(R.id.speedTv);
        downloadSizeTv = mDialog.findViewById(R.id.sizeTv);

        Window window = mDialog.getWindow();
        if (window != null) {
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public PercentageProgressBar(Context context, String title) {
        this(context);
        titleTv.setText(title);
    }

    public PercentageProgressBar(Context context, String title, int style) {
        this(context, title);
        if (style == HORIZONTAL) {

            speedSizeLl.setVisibility(View.VISIBLE);
            horizontalBar.setVisibility(View.VISIBLE);

            horizontalBar.setScaleY(3f);
            setDownloadMax(100);
            setDownloadProgress(0);

        } else if (style == CIRCULAR) {
            circularBar.setVisibility(View.VISIBLE);
        }
    }

    public void setStyle(int style) {

        if (style == HORIZONTAL) {

            circularBar.setVisibility(View.GONE);

            speedSizeLl.setVisibility(View.VISIBLE);
            horizontalBar.setVisibility(View.VISIBLE);

            horizontalBar.setScaleY(3f);
            setDownloadMax(100);
            setDownloadProgress(0);

        } else if (style == CIRCULAR) {
            circularBar.setVisibility(View.VISIBLE);

            speedSizeLl.setVisibility(View.GONE);
            horizontalBar.setVisibility(View.GONE);
        }
    }

    public void show() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    public void setTitle(String title) {
        titleTv.setText(title);
    }

    public void setDownloadProgress(int progress) {
        horizontalBar.setProgress(progress);
    }

    public void setDownloadMax(int maxProgress) {
        horizontalBar.setMax(maxProgress);
    }

    public void setDownloadSpeed(int speed) {

        final String downloadSpeed;
        if (speed == -1) {
            downloadSpeed = "";
        } else if (speed > 1000.0) {
            double KB = Double.parseDouble(String.valueOf(speed / 1000.0));
            if (KB > 1024.0) {
                double MB = Double.parseDouble(String.valueOf(KB / 1024.0));
                downloadSpeed = new DecimalFormat("###.###").format(MB) + " MB/s";
            } else {
                downloadSpeed = new DecimalFormat("###.###").format(KB) + " KB/s";
            }
        } else {
            downloadSpeed = speed + " B/s";
        }

        speedTv.setText(downloadSpeed);
    }

    public void setDownloadSize(long downloadSize, String fileLength) {

        String download;
        if (downloadSize == -1) {
            download = "";
        } else {
            if (downloadSize > 1000.0) {
                double KB = Double.parseDouble(String.valueOf(downloadSize / 1000.0));
                if (KB > 1024.0) {
                    double MB = Double.parseDouble(String.valueOf(KB / 1024.0));
                    download = new DecimalFormat("###.###").format(MB) + " MB";
                } else {
                    download = new DecimalFormat("###.###").format(KB) + " KB";
                }
            } else {
                download = downloadSize + " B";
            }

            if (!fileLength.isEmpty()) {
                download = download + "/" + fileLength;
            }
        }

        downloadSizeTv.setText(download);
    }

    public void dismiss() {
        try {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "dismiss", String.valueOf(e.getCause()));
        }
    }
}