package com.sixsimplex.treasurehunt.Phantom1.appfragment.dashboard;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import com.sixsimplex.treasurehunt.Phantom1.model.DeliveryDataModel;
import com.sixsimplex.treasurehunt.R;
import com.sixsimplex.treasurehunt.revelocore.data.Feature;

public class Board {
    public static TextView pendingCountView, completeCountView, inCompleteCountView, totalCountView;
    public static int pendingCount = 0, completeCount = 0, inCompleteCount = 0, totalCount = 0;
    public static Handler mHandler = new Handler(Looper.getMainLooper());
    @SuppressLint("StaticFieldLeak")

    public static void setBoard(View view) {
        try {
            if (view != null) {
                pendingCountView = view.findViewById(R.id.pending_cv);
                completeCountView = view.findViewById(R.id.complete_cv);
                inCompleteCountView = view.findViewById(R.id.incomplete_cv);
                totalCountView = view.findViewById(R.id.total_cv);
                setCount();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void setCount() {
        try {
            if (pendingCountView != null &&
                    completeCountView != null &&
                    inCompleteCountView != null &&
                    totalCountView != null) {
                pendingCountView.setText(String.valueOf(pendingCount));
                completeCountView.setText(String.valueOf(completeCount));
                inCompleteCountView.setText(String.valueOf(inCompleteCount));
                totalCountView.setText(String.valueOf(totalCount));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void updateDataSetChange() {
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    calCounts();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setCount();
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void calCounts() {
        try {
            if (DeliveryDataModel.getFeatureList() != null) {
                if (!DeliveryDataModel.getFeatureList().isEmpty()) {
                    clearCount();
                    for (Feature feature : DeliveryDataModel.getFeatureList()) {
//                        boolean isVisited = (boolean) feature.getAttributes().get("isvisited");
                        boolean isSkipped = (boolean) feature.getAttributes().get("skipped");
                        boolean isDelivered = (boolean) feature.getAttributes().get("isdelivered");
                        if (!isSkipped && isDelivered) {
                            completeCount++;
                        }
                        if (isSkipped && !isDelivered) {
                            inCompleteCount++;
                        }
                        if (!isSkipped && !isDelivered) {
                            pendingCount++;
                        }
                    }
                    totalCount = DeliveryDataModel.getFeatureList().size();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void clearCount() {
        pendingCount = 0;
        completeCount = 0;
        inCompleteCount = 0;
        totalCount = 0;
    }

}
