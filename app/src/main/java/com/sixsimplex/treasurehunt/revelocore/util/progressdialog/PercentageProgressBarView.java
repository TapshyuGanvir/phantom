package com.sixsimplex.treasurehunt.revelocore.util.progressdialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.util.log.ReveloLogger;

import java.text.DecimalFormat;

public class PercentageProgressBarView extends View {
    private Context mContext;
    private View mCustomView;
    private LayoutInflater mInflater;
    private TextView titleTv;
    private ProgressBar horizontalBar, circularBar;
    private LinearLayout speedSizeLl;
    private TextView speedTv;
    private TextView downloadSizeTv;

    public static final int HORIZONTAL = 0;
    public static final int CIRCULAR = 1;
    private String className = "PercentageProgressBarView";

    private String title;
    private int style;
    //Constructor
    public PercentageProgressBarView(Context context, String title) {
        this(context);
        this.title=title;
        this.style=CIRCULAR;
    }

    public PercentageProgressBarView(Context context, String title, int style) {
        this(context, title);
        this.title=title;
        this.style=style;

    }
    public PercentageProgressBarView(Context context) {
        super(context);
        mContext = context;
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView() {
        if(mCustomView==null) {
            mCustomView = mInflater.inflate(R.layout.percentage_progress_layout_view, null);
            //initialize all the child here
            horizontalBar = mCustomView.findViewById(R.id.horizontalBar);
            circularBar = mCustomView.findViewById(R.id.circularBar);

            titleTv = mCustomView.findViewById(R.id.titleTv);
            speedSizeLl = mCustomView.findViewById(R.id.speedSizeLl);
            speedTv = mCustomView.findViewById(R.id.speedTv);
            downloadSizeTv = mCustomView.findViewById(R.id.sizeTv);

            setTitle(title);
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
        return mCustomView;
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
            if(mCustomView!=null) {
                mCustomView.setVisibility(GONE);
                mCustomView = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ReveloLogger.error(className, "dismiss", String.valueOf(e.getCause()));
        }
    }
}
